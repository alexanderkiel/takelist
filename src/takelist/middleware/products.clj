(ns takelist.middleware.products
  (:require [takelist.db :as db]))

(defn wrap-products [handler db]
  (fn [{:keys [user] :as request}]
    (if user
      (handler (assoc request :products (db/list-products db)))
      (handler request))))
