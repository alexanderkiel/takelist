(ns takelist.middleware.store-order
  (:require [takelist.db :as db]))

(defn wrap-store-order
  "Returns a handler that expects a user and a product in the request.
  Therefore it has to be called after wrap-auth and wrap-product."
  [handler db]
  (fn [{:keys [product user params] :as request}]
    (assert user "Expected user in request.")
    (assert product "Expected product in request.")
    (let [order (db/create-order! db user product)]
      (handler (assoc request :order order)))))
