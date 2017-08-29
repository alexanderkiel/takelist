(ns takelist.system
  (:require
    [clojure.spec :as s]
    [com.stuartsierra.component :as component]
    [takelist.system.h2 :as h2]))

(s/def ::client-id
  string?)

(s/def ::client-secret
  string?)

(s/def ::base-uri
  string?)

(s/def ::h2-db-file
  string?)

(s/def ::env
  (s/keys
    :req-un
    [::client-id
     ::client-secret
     ::base-uri
     ::h2-db-file]))

(s/fdef system
  :args (s/cat :env ::env))

(defn system [{:keys [h2-db-file]}]
  (component/system-map
    :db (h2/h2 h2-db-file)))
