(ns takelist.middleware.db)

(defn wrap-db [handler db]
  (fn [request]
    (handler (assoc request :db db))))
