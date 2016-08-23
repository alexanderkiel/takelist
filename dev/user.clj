(ns user
  (:require [aleph.http :as http]
            [clojure.tools.namespace.repl :refer [refresh]]
            [takelist.core :refer [handler]]))

(def server (http/start-server handler {:port 8080}))

(comment
  (.close server)
  (refresh)
  )
