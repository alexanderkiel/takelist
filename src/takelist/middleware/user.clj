(ns takelist.middleware.user
  (:require [clojure.java.jdbc :as j]))

(defn wrap-user [handler db]
  (fn [request]
    (let [id "1"
          user (first (j/query db ["select * from tkl_user where id = ?" id]))]
      (handler (assoc request :user user)))))
