(ns takelist.spec
  (:require [clojure.spec :as s]
            [clojure.string :as str])
  (:import [java.util UUID]))

(set! *warn-on-reflection* true)

(s/def ::non-blank-str
  (s/and string? (complement str/blank?)))

(s/def :takelist.http.param/int
  (s/conformer (fn [x] (try (Integer/parseInt x) (catch Exception _ ::s/invalid)))))

(s/def :takelist.http.param/pos-int
  (s/and :takelist.http.param/int pos?))

(s/def :takelist.http.param/uuid
  (s/conformer (fn [x] (try (UUID/fromString x) (catch Exception _ ::s/invalid)))))

(s/def :takelist.http/error-status
  (s/with-gen #(s/int-in-range? 400 600 %)
              #(s/gen #{400 401 500})))

(s/def ::db
  some?)

(s/def ::ident
  (s/tuple keyword? some?))

;; ---- Domain Specific Specs -------------------------------------------------

(s/def :user/id
  uuid?)

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

(s/def :takelist/product
  (s/keys :req [:product/id :product/name]))

(s/def :order/id
  uuid?)

(s/def :order/product-id
  :product/id)

(s/def :order/product
  :takelist/product)

(s/def :order/user-id
  :user/id)

(s/def :order/order-date
  inst?)

(s/def :order/amount
  pos-int?)

(s/def :takelist/order
  (s/keys :req [:order/id :order/product-id :order/user-id :order/order-date :order/amount]))
