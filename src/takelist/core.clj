(ns takelist.core
  (:require [aleph.http :as http]
            [com.stuartsierra.component :as comp]
            [environ.core :refer [env]]
            [takelist.app :refer [app]]
            [takelist.db :as db])
  (:gen-class))

(def h2 (db/h2 (env :database-uri)))

(defn -main [& _]
  (let [server (http/start-server (app (assoc env :db (:db (comp/start h2))))
                                  {:port 8080})]
    (.wait-for-close server)))
