(ns takelist.app
  (:require [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.session :refer [wrap-session]]
            [takelist.handler :as h]
            [takelist.middleware.auth :refer [wrap-auth]]
            [takelist.middleware.content-type :refer [wrap-content-type]]
            [takelist.middleware.db :refer [wrap-db]]
            [takelist.middleware.product :refer [wrap-product]]
            [takelist.middleware.user :refer [wrap-user]]))

(defn routing-handler [conf]
  (fn [req]
    (case (:uri req)
      "/" ((wrap-user h/home-handler (:db conf)) req)
      "/order" ((-> h/order-form-handler
                    (wrap-product (:db conf))
                    (wrap-auth)
                    (wrap-user (:db conf))) req)
      "/post-order" ((-> h/order-post-handler
                         (wrap-product (:db conf))
                         (wrap-auth)
                         (wrap-user (:db conf))) req)
      "/oauth2-code" (h/oauth2-code-handler req)
      (h/not-found-handler req))))

(defn app
  "Whole application ring handler.

  Conf is a map of :db and other things."
  [conf]
  (-> (routing-handler conf)
      (wrap-content-type "text/html")
      (wrap-db (:db conf))
      (wrap-session)
      (wrap-keyword-params)
      (wrap-params)))
