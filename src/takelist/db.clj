(ns takelist.db
  (:require [clj-time.core :as time]
            [clj-time.coerce :as time-coerce]
            [clojure.java.jdbc :as j]
            [clojure.spec :as s]
            [clojure.spec.gen :as g]
            [clojure.string :as str]
            [takelist.spec]
            [takelist.util :as u])
  (:import [java.util UUID]))

(s/def ::db
  any?)

(s/def ::query-expr
  (s/or :property keyword? :join (s/map-of keyword? ::query)))

(s/def ::query
  (s/coll-of ::query-expr :kind vector? :min-count 1))

(s/fdef find-user-query
  :args (s/cat :props (s/and (s/coll-of keyword?) #(pos? (count %)))
               :constraints (s/map-of keyword? any?))
  :ret string?
  :fn #(and (= (count (filter #{\=} (:ret %)))
               (count (-> % :args :constraints)))))

(defn find-user-query [props constraints]
  (format
    "select %s from tkl_user %s %s"
    (str/join ", " (map name props))
    (if (seq constraints) "where" "")
    (str/join " and " (for [[key] constraints] (str (name key) " = ?")))))

(s/fdef find-user
  :args (s/cat :db ::db
               :props (s/coll-of keyword?)
               :constraints (s/keys :opt-un [:user/name :user/issuer :user/subject]))
  :ret map?)

(defn find-user
  "Searches for a unique user using the specified constraints and returns its
  properties as requested.

  Throws an exception if more than one user was found."
  [db props constraints]
  (let [sql-params (cons (find-user-query props constraints) (map second constraints))]
    (u/only (j/query db sql-params))))

(s/fdef create-user!
  :args (s/cat :db ::db :user (s/keys :req-un [:user/name :user/issuer :user/subject]))
  :ret uuid?)

(defn create-user! [db {:keys [name issuer subject]}]
  (assert name)
  (assert issuer)
  (assert subject)
  (let [id (UUID/randomUUID)]
    (j/insert! db "tkl_user" [:id :name :issuer :subject] [id name issuer subject])
    id))

(defn update-user! [db id props]
  (j/update! db "tkl_user" props ["id = ?" id])
  nil)

(s/fdef list-products
  :args (s/cat :db ::db)
  :ret (s/coll-of (s/keys :req-un [:product/id :product/name])))

(defn list-products [db]
  (j/query db ["SELECT id, name FROM tkl_product ORDER BY name"]))

(s/fdef create-order!
  :args (s/cat :db ::db
               :user (s/keys :req-un [:user/id])
               :product (s/keys :req-un [:product/id])
               :amount pos-int?)
  :ret :takelist/order)

(defn create-order! [db {user-id :id} {product-id :id} amount]
  (let [order-id (UUID/randomUUID)
        order-date (time/now)]
    (j/insert! db "tkl_order"
               [:id :product_id :user_id :order_date :amount]
               [order-id product-id user-id (time-coerce/to-date order-date) amount])
    #:order{:id order-id
            :product-id product-id
            :user-id user-id
            :order-date order-date
            :amount amount}))

(s/fdef find-order
  :args (s/cat :db ::db :id :order/id :query ::query)
  :ret (s/nilable :takelist/order))


(comment
  (second (first {:test :a}))
  (s/conform ::query [:order/amount {:order/product [:product/name]}])
  (def query [:order/amount :order/price {:order/product [:product/name {:product/owner [:owner/name]}]} {:order/supplier [:supplier/name :supplier/id]}])
  (def table "order")
  (def id "abc123")
  )

(defn pull-order [db id query]
  (j/query db ["SELECT * FROM tkl_order"]))

(defn build-query [table query id]
  (let [projection (->> query
                        (mapcat (fn [query-expr]
                                  (if (keyword? query-expr)
                                    [(name query-expr)]
                                    (let [[edge query] (first query-expr)]
                                      (->> (filter keyword? query)
                                           (map name)
                                           (map (fn [column] (str "tkl_" (name edge) "." column))))))))
                        (str/join ","))
        joins (->> (for [join (filter map? query)
                         :let [[edge] (first join)
                               foreign-key (str (name edge) "_id")
                               table (str "tkl_" (name edge))]]
                     (str "JOIN " table " ON " foreign-key " = " table ".id"))
                   (str/join " "))]
    (format "SELECT %s FROM %s %s WHERE id = %s" projection table joins id)))


(comment
  (def testVec [1 2 3 4 5])
  (mapcat (fn [q] [(+ 1 q)]) testVec)

  (map (fn [q] [(+ 1 q)]) testVec)

  (loop [i 1 lis []]
    (println i)
    (if (< i 10)
      (recur (inc i) (conj lis i))
      (conj lis i)))

  (range 10)
  )

(defn table-alias [path]
  (str/join "_" (map name path)))

(defn extract-projections
  "Extracts the different projections (attributes) from the given query and returns
 them as a vector."
  [path query]
  (mapcat
    (fn [expr]
      (if (keyword? expr)
        (if (seq path)
          (let [table (table-alias path)
                alias (str/join "_" (map name (conj path expr)))]
            [(str table "." (name expr) " AS " alias)])
          [(name expr)])
        (let [[edge query] (first expr)]
          (extract-projections (conj path edge) query))))
    query))


(defn extract-joins
  ""
  [path query]
  (transduce
    (filter map?)
    (completing
      (fn [ret expr]
        (let [[edge query] (first expr)
              foreign-key  (str (table-alias path) "." (name edge) "_id")
              table (str "tkl_" (name edge))]
          (apply conj ret (str "JOIN " table " AS " (table-alias (conj path edge)) " ON " foreign-key " = " (table-alias (conj path edge)) ".id") (extract-joins (conj path edge) query)))))
    []
    query))

(comment
  completing
  (extract-projections [:order] query)
  (extract-joins [:order] query))