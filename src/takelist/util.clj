(ns takelist.util)

(defn only
  "Like first but throws on more than one element."
  [coll]
  (when (next coll)
    (throw (Exception. "The collection contains more than one element.")))
  (first coll))
