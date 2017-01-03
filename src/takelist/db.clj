(ns takelist.db
  (:require [clojure.java.jdbc :as j]
            [clojure.string :as str]
            [takelist.util :as u])
  (:import [java.util UUID]))

(defn- find-user-query [props constraints]
  (format
    "select %s from tkl_user where %s"
    (str/join ", " (map name props))
    (str/join " and " (for [[key] constraints] (str (name key) " = ?")))))

(defn find-user
  "Searches for a unique user using the specified constraints and returns its
  properties as requested.

  Throws an exception if more than one user was found."
  [db props constraints]
  (let [sql-params (cons (find-user-query props constraints) (map second constraints))]
    (u/only (j/query db sql-params))))

(defn create-user! [db {:keys [name issuer subject]}]
  (assert name)
  (assert issuer)
  (assert subject)
  (let [id (UUID/randomUUID)]
    (j/insert! db "tkl_user" [:id :name :issuer :subject] [id name issuer subject])
    id))

(defn update-user! [db id props]
  (j/update! db "tkl_user" props ["id = ?" id])
  nil)
