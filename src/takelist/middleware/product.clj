(ns takelist.middleware.product
  (:require [clojure.java.jdbc :as j]))

(defn wrap-product [handler db]
  (fn [request]
    (let [params (:params request)
          id (:product-id params)
          product (first (j/query db ["select * from tkl_product where id = ?" id]))]
      (handler (assoc request :product product)))))
