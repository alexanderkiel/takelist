(ns takelist.system.h2
  (:require
    [clj-time.core :as time]
    [clj-time.coerce :as time-coerce]
    [clojure.java.jdbc :as j]
    [com.stuartsierra.component :refer [Lifecycle]]
    [takelist.crud.order :refer [OrderCrud]])
  (:import
    [java.util UUID]))

(def product-create-stmt
  (str "CREATE TABLE IF NOT EXISTS tkl_product ("
       "id uuid constraint tkl_product_pk primary key, name varchar)"))

(def create-user-stmt
  (str "CREATE TABLE IF NOT EXISTS tkl_user ("
       "id UUID constraint tkl_user_pk primary key"
       ", name varchar NOT NULL"
       ", issuer varchar NOT NULL"
       ", subject varchar NOT NULL"
       ", CONSTRAINT uq_iss_sub UNIQUE (issuer, subject)"
       ")"))

(def create-order-stmt
  (str "CREATE TABLE IF NOT EXISTS tkl_order ("
       "id UUID constraint tkl_order_pk PRIMARY KEY"
       ", product_id UUID NOT NULL REFERENCES tkl_product(id)"
       ", user_id UUID NOT NULL REFERENCES tkl_user(id)"
       ", order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL"
       ", amount INTEGER NOT NULL"
       ", CONSTRAINT amount_positive CHECK (amount > 0)"
       ")"))

(defrecord H2 [h2-db-file db]
  Lifecycle
  (start [this]
    (let [db {:classname "org.h2.Driver"
                 :subprotocol "h2:file"
                 :subname h2-db-file}]
      (j/execute! db product-create-stmt)
      (j/execute! db create-user-stmt)
      (j/execute! db create-order-stmt)
      (assoc this :db db)))
  (stop [this]
    (assoc this :db nil))

  OrderCrud
  (create- [_ {:keys [order/amount] user-id :user/id product-id :product/id}]
    (let [order-id (UUID/randomUUID)
          order-date (time/now)]
      (j/insert! db "tkl_order"
                 [:id :product_id :user_id :order_date :amount]
                 [order-id product-id user-id (time-coerce/to-date order-date)
                  amount])
      #:order{:id order-id
              :product-id product-id
              :user-id user-id
              :order-date order-date
              :amount amount})))

(defn h2 [h2-db-file]
  (map->H2 {:h2-db-file h2-db-file}))

(comment
  (def db (com.stuartsierra.component/start (h2 "~/.takelist/db-test")))
  (takelist.crud.order/create- db {})
  (clojure.repl/pst)
  )
