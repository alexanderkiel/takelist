(ns takelist.pull
  (:require [clojure.java.jdbc :as j]
            [clojure.spec :as s]
            [clojure.spec.gen :as g]
            [clojure.string :as str]
            [takelist.spec :as spec])
  (:refer-clojure :exclude [alias]))

;; ---- Query-Specs -----------------------------------------------------------

(s/def ::query-expr
  (s/or :property keyword? :join (s/map-of keyword? ::query)))

(s/def ::query
  (s/coll-of ::query-expr :kind vector? :min-count 1))

;; ---- Internal-Specs --------------------------------------------------------

(s/def ::path
  string?)

;; ---- SQL-Specs -------------------------------------------------------------

(s/def ::table-name
  string?)

(s/def ::alias
  string?)

;; Forward declaration
(s/def ::table-expr
  any?)

(s/def ::l-expr
  ::table-expr)

(s/def ::r-expr
  ::table-expr)

(s/def ::on-cond
  string?)

(defmulti table-expr-spec :type)

(defmethod table-expr-spec :table
  [_]
  (s/keys :req-un [::table-name] :opt-un [::alias]))

(defmethod table-expr-spec :join-op
  [_]
  (s/keys :req-un [::l-expr ::r-expr ::on-cond]))

(s/def ::table-expr
  (s/multi-spec table-expr-spec :type))

(defn sql-name [expr]
  (str/replace (name expr) \- \_))

(defn alias [path expr]
  (str path "_" (sql-name expr)))

(s/fdef extract-projections
  :args (s/cat :path ::path :query ::query))

(defn extract-projections
  "Extracts the different projections (attributes) from the given query and returns
 them as a vector."
  [path query]
  (mapcat
    (fn [expr]
      (if (keyword? expr)
        (if (seq path)
          (let [table path
                alias (alias path expr)]
            [(str table "." (sql-name expr) " AS " alias)])
          [(name expr)])
        (let [[edge query] (first expr)]
          (extract-projections (str path "_" (sql-name edge)) query))))
    query))

(defn primary-key
  "The primary key of each table is called 'id' by convention."
  [table]
  (str table ".id"))

(defn foreign-key
  "The foreign key ends with '_id' by convention."
  [table join-key]
  (str table "." (name join-key) "_id"))

(defn table-name
  "All tables start with 'tkl_' by convention."
  [join-key]
  (str "tkl_" (name join-key)))

(defmulti path :type)

(defmethod path :table
  [{:keys [alias]}]
  alias)

(defmethod path :join-op
  [{:keys [r-expr]}]
  (path r-expr))

(s/fdef join-op*
  :args (s/cat :table-expr ::table-expr :path ::path :join-key keyword?))

(defn join-op* [table-expr path join-key]
  (let [foreign-key (foreign-key path join-key)
        table-alias (str path "_" (name join-key))]
    {:type :join-op
     :l-expr table-expr
     :r-expr
     {:type :table
      :table-name (table-name join-key)
      :alias table-alias}
     :on-cond
     (format "ON %s = %s" foreign-key (primary-key table-alias))}))

(defn- join? [x]
  (map? x))

(s/fdef join-op
  :args (s/cat :table-expr ::table-expr :query ::query)
  :ret ::table-expr)

(defn join-op
  "Joins the given table with tables as defined in the given query."
  [table-expr query]
  (let [path (path table-expr)]
    (transduce
      (filter join?)
      (completing
        (fn step [ret expr]
          (let [[join-key query] (first expr)]
            (join-op (join-op* ret path join-key) query))))
      table-expr
      query)))

(defmulti to-sql :type)

(defmethod to-sql :table
  [{:keys [table-name alias]}]
  (cond-> table-name
          alias (str " AS " alias)))

(defmethod to-sql :join-op
  [{:keys [l-expr r-expr on-cond]}]
  (str (to-sql l-expr) " JOIN " (to-sql r-expr) " " on-cond))

(s/fdef build-query
  :args (s/cat :table-key keyword? :query ::query))

(defn build-query [table-key query]
  (let [path (str "_" (name table-key))]
    (format
      "SELECT %s FROM %s WHERE %s.id = ?"
      (str/join ", " (extract-projections path query))
      (to-sql (join-op {:type :table
                        :table-name (table-name table-key)
                        :alias path}
                       query))
      path)))

(defn- build-result* [path query row]
  (reduce
    (fn [ret expr]
      (if (join? expr)
        (let [[join-key query] (first expr)]
          (if-let [val (build-result* (str path "_" (name join-key)) query row)]
            (assoc ret join-key val)
            ret))
        (if-let [val (get row (keyword (alias path expr)))]
          (assoc ret expr val)
          ret)))
    nil
    query))

(s/fdef build-result
  :args (s/cat :table-key keyword? :query ::query :row map?))

(defn build-result [table-key query row]
  (let [path (str "_" (name table-key))]
    (build-result* path query row)))

(s/fdef pull
  :args (s/cat :db ::spec/db :query ::query :ident ::spec/ident))

(defn pull
  "Returns a hierarchical selection of properties regarding query for entity
  with id."
  [db query [key id]]
  (let [table-key (-> key namespace keyword)
        build-result #(build-result table-key query %)]
    (j/query db [(build-query table-key query) id] {:row-fn build-result})))
