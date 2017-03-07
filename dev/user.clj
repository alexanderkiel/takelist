(ns user
  (:require [aleph.http :as http]
            [clojure.java.jdbc :as j]
            [clojure.spec.test :as st]
            [clojure.tools.namespace.repl :refer [refresh]]
            [environ.core :refer [env]]
            [takelist.app :refer [app]]
            [clojure.spec :as s])
  (:import [java.util UUID]))

(st/instrument)

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
  (j/execute! db "CREATE TABLE tkl_product (id uuid primary key, name varchar)")
  (j/execute! db (str "CREATE TABLE tkl_user (id UUID primary key"
                      ", name varchar NOT NULL"
                      ", issuer varchar NOT NULL"
                      ", subject varchar NOT NULL"
                      ", CONSTRAINT uq_iss_sub UNIQUE (issuer, subject)"
                      ")"))
  (j/execute! db (str "CREATE TABLE tkl_order (id UUID PRIMARY KEY"
                      ", product_id UUID NOT NULL REFERENCES tkl_product(id)"
                      ", user_id UUID NOT NULL REFERENCES tkl_user(id)"
                      ", order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL"
                      ", amount INTEGER NOT NULL"
                      ", CONSTRAINT amount_positive CHECK (amount > 0)"
                      ")"))
)

(comment
  (j/insert! db "tkl_product" [:id :name] [(UUID/randomUUID) "Kaffee"])
  (j/insert! db "tkl_product" [:id :name] [(UUID/randomUUID) "Club Mate"])
  (j/insert! db "tkl_user" [:id :name :issuer :subject] ["2" "dummy-user" "https://accounts.google.com" "tes"])
  )

(comment
  (j/query db ["select * from tkl_product"])
  (j/query db ["select * from tkl_user"])
  (j/query db ["select * from tkl_order"])
  )
