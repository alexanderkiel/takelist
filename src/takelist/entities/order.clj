(ns takelist.entities.order
  (:require
    [clj-time.core :refer [DateTimeProtocol]]
    [clojure.spec :as s]
    [takelist.entities.product]
    [takelist.entities.user]))

(s/def :order/id
  uuid?)

(s/def :order/product-id
  :product/id)

(s/def :order/user-id
  :user/id)

(s/def :order/order-date
  #(satisfies? DateTimeProtocol %))

(s/def :order/amount
  pos-int?)

(s/def :takelist/order
  (s/keys :req [:order/id :order/product-id :order/user-id :order/order-date :order/amount]))
