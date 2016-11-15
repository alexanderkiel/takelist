(ns user
  (:require [aleph.http :as http]
            [clojure.java.jdbc :as j]
            [clojure.tools.namespace.repl :refer [refresh]]
            [takelist.app :refer [app]])
  (:import [java.util UUID]))

(def db {:classname "org.h2.Driver"
         :subprotocol "h2:file"
         :subname "~/.takelist/db"})

(def server nil)

(defn init []
  (alter-var-root #'server (fn [_] (http/start-server (app {:db db}) {:port 8080}))))

(comment
  (init)
  (.close server)
  (refresh :after 'user/init)
  )


;; Database schema
(comment
  (j/execute! db "DROP TABLE tkl_order")
  (j/execute! db "DROP TABLE tkl_product")
  (j/execute! db "DROP TABLE tkl_user")
  (j/execute! db "CREATE TABLE tkl_product (id varchar(36) primary key, name varchar)")
  (j/execute! db "CREATE TABLE tkl_user (id varchar(36) primary key, name varchar)")
  (j/execute! db (str "CREATE TABLE tkl_order (id varchar(36) primary key"
  ",product_id varchar(36) NOT NULL REFERENCES tkl_product(id)"
  ",user_id varchar(36) NOT NULL REFERENCES tkl_user(id)"
  ",order_date timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL"
  ")"))
)

(comment
  (j/insert! db "tkl_product" [:id :name] ["1" "Kaffee"])
  (j/insert! db "tkl_product" [:id :name] ["2" "Club Mate"])
  (j/insert! db "tkl_user" [:id :name] ["1" "dummy-user"])
  )

(comment
  (j/query db ["select * from tkl_product"])
  )
