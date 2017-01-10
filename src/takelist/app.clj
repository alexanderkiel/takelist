(ns takelist.app
  (:require [bidi.bidi :as bidi]
            [bidi.ring :as bidi-ring]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.session :refer [wrap-session]]
            [takelist.handler :as h]
            [takelist.middleware.auth :refer [wrap-auth]]
            [takelist.middleware.content-type :refer [wrap-content-type]]
            [takelist.middleware.db :refer [wrap-db]]
            [takelist.middleware.product :refer [wrap-product]]
            [takelist.middleware.user :refer [wrap-user]]
            [takelist.route :refer [routes]]))

(defn wrap-not-found [handler]
  (fn [req]
    (if-let [resp (handler req)]
      resp
      (h/not-found-handler req))))

(defn wrap-path-for [handler path-for]
  (fn [req]
    (handler (assoc req :path-for path-for))))

(defn path-for [base-uri routes]
  (fn [handler & params]
    (str base-uri (apply bidi/path-for routes handler params))))

(defn app
  "Whole application ring handler.

  Conf is a map of :base-uri, :db and other things."
  [{:keys [base-uri db] :as conf}]
  (-> (bidi-ring/make-handler routes (h/handlers conf))
      (wrap-db db)
      (wrap-session)
      (wrap-keyword-params)
      (wrap-params)
      (wrap-not-found)
      (wrap-path-for (path-for base-uri routes))
      (wrap-content-type "text/html")))
