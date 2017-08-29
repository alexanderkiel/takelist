(ns takelist.core
  (:require [aleph.http :as http]
            [environ.core :refer [env]]
            [takelist.app :refer [app]])
  (:gen-class))


(def db {:classname "org.h2.Driver"
         :subprotocol "h2:file"
         :subname (env :database-uri)})

(def base-uri (env :base-uri))

(def client-id (env :client-id))

(def client-secret (env :client-secret))

(defn -main [& args]
  (let [server (http/start-server (app (assoc env :db db
                                                :base-uri base-uri
                                                :client-id client-id
                                                :client-secret client-secret))
                                {:port 8080})]
    (.wait-for-close server)))
