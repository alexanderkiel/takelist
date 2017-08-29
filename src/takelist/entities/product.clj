(ns takelist.entities.product
  (:require
    [clojure.spec :as s]))

(s/def :product/id
  uuid?)

(s/def :product/order-params
  (s/keys :req [:user/id :product/id :order/amount]))
