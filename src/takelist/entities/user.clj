(ns takelist.entities.user
  (:require
    [clojure.spec :as s]))

(s/def :user/id
  uuid?)
