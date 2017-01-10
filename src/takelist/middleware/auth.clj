(ns takelist.middleware.auth
  (:require [ring.util.response :refer [redirect]]))

(defn wrap-auth [handler]
  (fn [request]
    (if (:user request)
      (handler request)
      (redirect ((:path-for request) :home)))))
