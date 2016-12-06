(ns takelist.app
  (:require [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.params :refer [wrap-params]]
            [takelist.handler :as h]
            [takelist.middleware.content-type :refer [wrap-content-type]]
            [takelist.middleware.product :refer [wrap-product]]
            [takelist.middleware.user :refer [wrap-user]]))

(defn routing-handler [req]
  (case (:uri req)
    "/" (h/home-handler req)
    "/order" (h/order-form-handler req)
    "/post-order" (h/order-post-handler req)
    "/oauth2-code" (h/oauth2-code-handler req)
    (h/not-found-handler req)))

(defn app
  "Whole application ring handler.

  Conf is a map of :db and other things."
  [conf]
  (-> routing-handler
      (wrap-content-type "text/html")
      (wrap-product (:db conf))
      (wrap-user (:db conf))
      wrap-keyword-params
      wrap-params))
