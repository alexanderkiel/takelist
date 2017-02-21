(ns takelist.middleware.store-order
  (:require [takelist.db :as db]
            [clojure.spec :as s]
            [ring.util.response :as ring]
            [takelist.spec :as spec]))

(defn wrap-store-order
  "Returns a handler that expects a user and a product in the request.
  Therefore it has to be called after wrap-auth and wrap-product."
  [handler db]
  (fn [{:keys [product user params] :as request}]
    (assert user "Expected user in request.")
    (assert product "Expected product in request.")
    (let [amount (s/conform ::spec/parse-pos-int (:amount params))]
      (if (s/invalid? amount)
        (-> (ring/response (str "Invalid amount (" (:amount params) ")."))
            (ring/content-type "text/plain")
            (ring/status 400))
        (let [order (db/create-order! db user product amount)]
          (handler (assoc request :order order)))))))