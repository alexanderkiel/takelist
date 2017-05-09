(ns takelist.middleware.order
  (:require [takelist.db :as db]
            [clojure.spec :as s]
            [ring.util.response :as ring]
            [takelist.spec]
            [takelist.util :as u]))

(defn wrap-store-order
  "Returns a handler that expects a user and a product in the request.
  Therefore it has to be called after wrap-auth and wrap-product."
  [handler db]
  (fn [{:keys [product user params] :as request}]
    (assert user "Expected user in request.")
    (assert product "Expected product in request.")
    (let [amount (s/conform :takelist.http.param/pos-int (:amount params))]
      (if (s/invalid? amount)
        (u/error-resp 400 (str "Invalid amount (" (:amount params) ")."))
        (let [order (db/create-order! db user product amount)]
          (handler (assoc request :order order)))))))

(defn wrap-user-order
  [handler db query]
  (fn [{:keys [params] :as request}]
    (let [order-id (s/conform :takelist.http.param/uuid (:order-id params))]
      (if (s/invalid? order-id)
        (u/error-resp 400 (str "Invalid order-id (" (:order-id params) ")."))
        (if-let [order (db/find-order db query order-id)]
          (handler (assoc request :order order))
          (u/error-resp 409 (str "Unknown order with id " (:order-id params) ".")))))))
