(ns takelist.adapters.ring
  (:require
    [clojure.spec :as s]
    [cognitect.anomalies :as anom]
    [ring.util.response :as ring]
    [takelist.use-cases.product :as product]
    [takelist.util :as u]))

(s/fdef error-resp
  :args (s/cat :status :takelist.http/error-status
               :msg string?)
  :ret map?
  :fn #(= (-> % :args :status)
          (-> % :ret :status)))

(defn error-resp [status msg]
  (-> (ring/response (or msg "Unbekannter Fehler"))
      (ring/content-type "text/plain")
      (ring/status status)))

(s/fdef error-resp-anom
  :args (s/cat :anomaly ::anom/anomaly)
  :ret map?)

(defmulti error-resp-anom ::anom/category)

(defmethod error-resp-anom ::anom/conflict
  [{:keys [::anom/message]}]
  (error-resp 409 message))

(defmethod error-resp-anom :default
  [{:keys [::anom/message]}]
  (error-resp 500 message))

(defn- redirect-to-order [path-for {:keys [order/id]}]
  (ring/redirect (str (path-for :order-confirmation) "?order-id=" id)))

(s/fdef post-order-handler
  :args (s/cat :context #(uuid? (get-in % [:session :user-id]))))

(defn post-order-handler [{:keys [path-for] :as context}]
  (fn [{:keys [session params]}]
    (let [product-id (s/conform :takelist.http.param/uuid (:product-id params))
          amount (s/conform :takelist.http.param/pos-int (:amount params))]
      (cond
        (s/invalid? product-id)
        (u/error-resp 400 (str "Invalid product id (" (:product-id params) ")."))
        (s/invalid? amount)
        (u/error-resp 400 (str "Invalid amount (" (:amount params) ")."))
        :else
        ;; diese sache als macro oder funktion abstrahieren
        (let [{:keys [::anom/category] :as order}
              (product/order context {:user/id (:user-id session)
                                      :product/id product-id
                                      :order/amount amount})]
          (if category
            (error-resp-anom order)
            (redirect-to-order path-for order)))
        (comment
          (anom->> (product/order context {:user/id (:user-id session)
                                           :product/id product-id
                                           :order/amount amount})
                   (redirect-to-order path-for)))))))
