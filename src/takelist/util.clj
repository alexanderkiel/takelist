(ns takelist.util
  (:require [buddy.core.codecs :as codecs]
            [buddy.core.codecs.base64 :as b64]
            [cheshire.core :as json]
            [clojure.string :as str]))

(defn only
  "Like first but throws on more than one element."
  [coll]
  (when (next coll)
    (throw (Exception. "The collection contains more than one element.")))
  (first coll))

(defn unsafe-unsign
  "Unsign JWT without verifying the signature or validating claims.

  Use only when the origin of the token is known!"
  [token]
  (let [[_ payload] (str/split token #"\." 3)]
    (-> (b64/decode payload)
        (codecs/bytes->str)
        (json/parse-string keyword))))
