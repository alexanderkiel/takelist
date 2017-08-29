(ns takelist.system.h2-test
  (:require
    [clojure.java.jdbc :as j]
    [clojure.spec :as s]
    [clojure.spec.test :as st]
    [clojure.test :refer :all]
    [com.gfredericks.test.chuck.clojure-test :refer [checking]]
    [com.stuartsierra.component :as component]
    [juxt.iota :refer [given]]
    [takelist.system.h2 :refer [h2]]
    [takelist.crud.order :as order-crud])
  (:import
    [java.util UUID]))

(st/instrument)

(def db nil)

(use-fixtures
  :each
  (fn [f]
    (alter-var-root #'db (fn [_] (component/start (h2 (str "~/.takelist/db-" (UUID/randomUUID))))))
    (f)))

(deftest create-order-test
  (checking "Create an order" 10
    [user-id (s/gen :user/id)
     issuer (s/gen :user/issuer)
     subject (s/gen :user/subject)
     product-id (s/gen :product/id)
     amount (s/gen :order/amount)]
    (j/insert! (:db db) "tkl_user" [:id :name :issuer :subject] [user-id "name-193651" issuer subject])
    (j/insert! (:db db) "tkl_product" [:id :name] [product-id "name-193926"])
    (let [params {:user/id user-id
                  :product/id product-id
                  :order/amount amount}
          order (order-crud/create db params)]
      (is (s/valid? :takelist/order order))
      (given order
        :order/product-id := product-id
        :order/user-id := user-id
        :order/amount := amount)
      (is (not (empty? (j/query (:db db) ["select 1 from tkl_order where (product_id, user_id, amount) = (?, ?, ?)" product-id user-id amount])))))))
