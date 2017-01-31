(ns takelist.spec
  (:require [clojure.spec :as s]
            [clojure.string :as str]))

(s/def ::non-blank-str
  (s/and string? (complement str/blank?)))

(s/def :user/name
  ::non-blank-str)

(s/def :user/issuer
  ::non-blank-str)

(s/def :user/subject
  ::non-blank-str)

(s/def :product/id
  uuid?)

(s/def :product/name
  ::non-blank-str)
