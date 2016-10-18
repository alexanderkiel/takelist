(ns takelist.middleware.product)

(def dummy-product
  {:id "abc123"
   :name "Dummy-Product"})

(defn wrap-product [handler]
  (fn [request]
    (handler (assoc request :product dummy-product))))
