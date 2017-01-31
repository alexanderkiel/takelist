(ns user
  (:require [aleph.http :as http]
            [clojure.java.jdbc :as j]
            [clojure.tools.namespace.repl :refer [refresh]]
            [environ.core :refer [env]]
            [takelist.app :refer [app]]
            [clojure.spec :as s]))

(def db {:classname "org.h2.Driver"
         :subprotocol "h2:file"
         :subname "~/.takelist/db"})

(defonce server nil)

(defn init []
  (alter-var-root #'server (fn [_] (http/start-server (app (assoc env :db db)) {:port 8080}))))

(defn reload []
  (.close server)
  (refresh :after 'user/init))

(comment
  (init)
  (reload)
  )


;; Database schema
(comment
  (j/execute! db "DROP TABLE tkl_order")
  (j/execute! db "DROP TABLE tkl_product")
  (j/execute! db "DROP TABLE tkl_user")
  (j/execute! db "CREATE TABLE tkl_product (id varchar(36) primary key, name varchar)")
  (j/execute! db (str "CREATE TABLE tkl_user (id varchar(36) primary key"
                      ", name varchar NOT NULL"
                      ", issuer varchar NOT NULL"
                      ", subject varchar NOT NULL"
                      ", CONSTRAINT uq_iss_sub UNIQUE (issuer, subject)"
                      ")"))
  (j/execute! db (str "CREATE TABLE tkl_order (id varchar(36) primary key"
  ", product_id varchar(36) NOT NULL REFERENCES tkl_product(id)"
  ", user_id varchar(36) NOT NULL REFERENCES tkl_user(id)"
  ", order_date timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL"
  ")"))
)

(comment
  (j/insert! db "tkl_product" [:id :name] ["1" "Kaffee"])
  (j/insert! db "tkl_product" [:id :name] ["2" "Club Mate"])
  (j/insert! db "tkl_user" [:id :name :issuer :subject] ["2" "dummy-user" "https://accounts.google.com" "tes"])
  )

(comment
  (j/query db ["select * from tkl_product"])
  (j/query db ["select * from tkl_user"])
  )
