(ns takelist.core
  (:require [aleph.http :as http]))

(defn handler [req]
  {:status 200
   :body "Hello, World!"})

(defn -main [& args]
  (println "Starting webserver...")
  (http/start-server handler {:port 8080})
  (println "done.")
  (Thread/sleep 60000))
