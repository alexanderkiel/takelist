(ns user
  (:require [aleph.http :as http]
            [clojure.java.jdbc :as j]
            [clojure.tools.namespace.repl :refer [refresh]]
            [takelist.app :refer [app]]))

(def db {:classname "org.h2.Driver"
         :subprotocol "h2:file"
         :subname "~/.takelist/db"})

(def server (http/start-server (app {:db db}) {:port 8080}))

(comment
  (.close server)
  (refresh)
  )


;; Database schema
(comment
  (j/execute! db "DROP TABLE product")
  (j/execute! db "CREATE TABLE product (id varchar primary key, name varchar)")
)

(comment
  (j/insert! db "product" [:id :name] ["1" "Kaffee"])
  (j/insert! db "product" [:id :name] ["2" "Club Mate"])
  )

(comment
  (j/query db ["select * from product"])
  )
