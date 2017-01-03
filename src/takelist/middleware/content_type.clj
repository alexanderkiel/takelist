(ns takelist.middleware.content-type
  (:require [ring.util.response :as rur]))

(defn wrap-content-type [handler content-type]
  (fn [request]
    (-> (handler request)
        (rur/content-type content-type))))
