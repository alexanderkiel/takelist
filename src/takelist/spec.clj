(ns takelist.spec
  (:require [clojure.spec :as s]
            [clojure.string :as str]))

(set! *warn-on-reflection* true)

(s/def ::non-blank-str
  (s/and string? (complement str/blank?)))

(s/def ::parse-int
  (s/conformer (fn [x] (try (Integer/parseInt x) (catch Exception _ ::s/invalid)))))

(s/def ::parse-pos-int
  (s/and ::parse-int pos?))

(s/def :user/id
  ::non-blank-str)

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

(s/def :order/id
  uuid?)

(s/def :order/product-id
  :product/id)

(s/def :order/user-id
  :user/id)

(s/def :order/order-date
  inst?)

(s/def :order/amount
  pos-int?)

(s/def :takelist/order
  (s/keys :req [:order/id :order/product-id :order/user-id :order/order-date :order/amount]))

