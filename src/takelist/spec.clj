(ns takelist.spec
  (:require [clojure.spec :as s]
            [clojure.string :as str]))

(s/def ::non-blank-str
  (s/and string? (complement str/blank?)))

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

(s/def :takelist/order
  (s/keys :req [:order/id :order/product-id :order/user-id :order/order-date]))

