(ns user
  (:require [aleph.http :as http]
            [clojure.tools.namespace.repl :refer [refresh]]
            [takelist.app :refer [app]]))

(def server (http/start-server app {:port 8080}))

(comment
  (.close server)
  (refresh)
  )
