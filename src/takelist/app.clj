(ns takelist.app
  (:require [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.params :refer [wrap-params]]
            [takelist.handler :as h]))

(defn routing-handler [req]
  (case (:uri req)
    "/" (h/order-form-handler req)
    "/post-order" (h/order-post-handler req)
    (h/not-found-handler req)))

(def app
  (-> routing-handler
      wrap-keyword-params
      wrap-params))
