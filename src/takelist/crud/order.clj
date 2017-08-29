(ns takelist.crud.order
  (:require
    [clojure.spec :as s]
    [takelist.entities.order]
    [takelist.entities.product]
    [takelist.entities.user]))

(defprotocol OrderCrud
  (create- [this params]))

(s/fdef create
  :args (s/cat :crud #(satisfies? OrderCrud %)
               :params (s/keys :req [:user/id :product/id :order/amount]))
  :ret :takelist/order)

(defn create [crud params]
  (create- crud params))

(comment
  (clojure.spec.test/instrument)

  (create (reify OrderCrud (create- [this params])) {})

  )
