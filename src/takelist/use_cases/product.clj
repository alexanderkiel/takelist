(ns takelist.use-cases.product
  (:require
    [clojure.spec :as s]
    [cognitect.anomalies :as anom]
    [takelist.entities.order]
    [takelist.crud.order :as order-crud]))

(s/fdef order
  :args (s/cat :context some?
               :params (s/keys :req [:user/id :product/id :order/amount]))
  :ret (s/or :takelist/order ::anom/anomaly))

(defn order [context params]
  (if (product-crud/find-by-id (get context :product-crud) (:product/id params))
    (order-crud/create (get context :order-crud) params)
    {::anom/category ::anom/conflict
     ::anom/message "Produkt nicht gefunden"}))
