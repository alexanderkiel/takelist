(ns takelist.middleware.user
  (:require [takelist.db :as db]))

(defn- find-user [db {:keys [user-id]}]
  (when user-id
    (db/find-user db [:id :name] {:id user-id})))

(defn wrap-user
  "Assocs :user to request if the session contains a :user-id."
  [handler db]
  (fn [{:keys [session] :as request}]
    (if-let [user (find-user db session)]
      (handler (assoc request :user user))
      (handler request))))
