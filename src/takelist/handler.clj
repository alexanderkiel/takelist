(ns takelist.handler
  "Here are all our handlers."
  (:require [clojure.pprint]
            [clojure.string :as str]
            [hiccup.core :refer [html]]))

(defn head
  "Generated the HTML head."
  [& title-parts]
  [:head
   [:meta {:charset "utf-8"}]
   [:title (str/join " - " (conj (vec (reverse title-parts)) "TakeList"))]
   [:link {:rel "stylesheet"
           :href "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css"
           :integrity "sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u"
           :crossorigin "anonymous"}]])

(defn order-form-handler [req]
  {:status 200
   :body
   (html
     [:html
      (head)
      [:body
       [:div {:class "container"}
        [:div {:class "row"}
         [:div {:class "col-xs-12 col-xs-offset-0 col-sm-4 col-sm-offset-4"}
          [:form {:action "/post-order" :method "post"}
           [:div {:class "form-group"}
            [:p "Hiermit bestelle ich ein TODO."]
            [:input {:type "hidden" :name "product-id" :value "dummy"}]]
           [:div {:class "form-group"}
            [:label {:for "amount"} "Anzahl"]
            [:select {:id "amount" :name "amount" :class "form-control"}
             (for [x (range 1 6)]
               [:option {:value x} x])]]
           [:button {:type "submit" :class "btn btn-primary"} "Ok"]]]]]]])})

(defn order-post-handler [{:keys [params]}]
  {:status 200
   :body
   (html
     [:html
      (head)
      [:body
       [:p (let [{:keys [product-id amount]} params]
             (format "Vielen Dank für das Bestellen von %s %s." amount product-id))]]])})

(defn not-found-handler [req]
  {:status 404
   :body
   (html
     [:html
      (head "Not Found" "1")
      [:body
       [:p "Oppps... Page not found."]]])})
