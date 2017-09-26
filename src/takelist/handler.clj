(ns takelist.handler
  "Here are all our handlers."
  (:require [aleph.http :as http]
            [cheshire.core :as json]
            [clj-time.core :as time]
            [clj-time.format :as time-format]
            [clojure.java.jdbc :as j]
            [clojure.pprint :refer [pprint]]
            [clojure.string :as str]
            [environ.core :refer [env]]
            [hiccup.core :refer [html]]
            [ring.util.response :as r]
            [takelist.db :as db]
            [takelist.middleware.auth :refer [wrap-auth]]
            [takelist.middleware.product :refer [wrap-product]]
            [takelist.middleware.products :refer [wrap-products]]
            [takelist.middleware.order :refer [wrap-store-order wrap-user-order]]
            [takelist.middleware.user :refer [wrap-user]]
            [takelist.util :as u]))

(defn head
  "Generated the HTML head."
  [path-for & title-parts]
  (assert path-for)
  [:head
   [:meta {:charset "utf-8"}]
   [:title (str/join " - " (conj (vec (reverse title-parts)) "TakeList"))]
   [:link {:rel "stylesheet"
           :href "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css"
           :integrity "sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u"
           :crossorigin "anonymous"}]
   [:script {:src "//ajax.googleapis.com/ajax/libs/jquery/1.8.2/jquery.min.js"}]
   [:script {:src "https://apis.google.com/js/client:platform.js?onload=start" :async "true" :defer "true"}]
   [:script
    (format "function start() {
      gapi.load('auth2', function() {
        auth2 = gapi.auth2.init({
          client_id: '%s'
        });
      });
    }
    function signInCallback(authResult) {
      if (authResult['code']) {

        // Send the code to the server
        $.ajax({
          type: 'POST',
          url: '%s',
          contentType: 'application/octet-stream; charset=utf-8',
          success: function(result) {
            // Handle or verify the server response.
            location.reload(true);
          },
          processData: false,
          data: authResult['code']
        });
      } else {
        // There was an error.
      }
    }
    " (:client-id env) (path-for :oauth2-code))]])

(defn- order-path [path-for id]
  (str (path-for :order) "?product-id=" id))

(defn- product-list [products path-for]
  [:div
   [:h1 "Produktliste"]
   [:div {:class "list-group"}
    (for [{:keys [id name]} products]
      [:a {:class "list-group-item" :href (order-path path-for id)} name])]])

(defn- order-list [orders path-for]
  [:div
   [:h1 "Meine Bestellungen"]
   [:div {:class "list-group"}
    (for [{:keys [order/id order/product-id]} orders]
      [:a {:class "list-group-item" :href (order-path path-for id)} name])]])

(defn home-handler [{:keys [user path-for products user-orders]}]
  {:status 200
   :body
   (html
     [:html
      (head path-for)
      [:body
       [:div {:class "container"}
        [:div {:class "row"}
         [:div {:class "col-xs-4 col-xs-offset-4"}
          (if user
            [:div
             (product-list products path-for)
             (order-list user-orders path-for)]
            [:button {:id "signinButton"} "Mit Google einloggen"])
          [:script
           "$('#signinButton').click(function() {
             // signInCallback defined in step 6.
              auth2.grantOfflineAccess({'redirect_uri': 'postmessage'}).then(signInCallback);
                });"]]]]]])})

(defn order-form-handler [{:keys [product user path-for]}]
  {:status 200
   :body
   (html
     [:html
      (head path-for)
      [:body
       [:div {:class "container"}
        [:div {:class "row"}
         [:div {:class "col-xs-12 col-xs-offset-0 col-sm-4 col-sm-offset-4"}
          [:form {:action "/post-order" :method "post"}
           [:div {:class "form-group"}
            [:p (format "Hiermit bestelle ich (%s) %s." (:name user) (:name product))]
            [:input {:type "hidden" :name "product-id" :value (:id product)}]]
           [:div {:class "form-group"}
            [:label {:for "amount"} "Anzahl"]
            [:select {:id "amount" :name "amount" :class "form-control"}
             (for [x (range 1 6)]
               [:option {:value x} x])]]
           [:button {:type "submit" :class "btn btn-primary"} "Ok"]]]]]]])})

(defn- to-time-str [date]
  (-> (:hour-minute time-format/formatters)
      (time-format/with-zone (time/time-zone-for-id "Europe/Berlin"))
      (time-format/unparse date)))

(defn order-post-handler [{:keys [path-for order]}]
  (r/redirect (str (path-for :order-confirmation) "?order-id=" (:order/id order))))

(defn order-confirmation-handler [{:keys [path-for order]}]
  {:status 200
   :body
   (html
     [:html
      (head path-for)
      [:body
       [:p (format "Vielen Dank für das Bestellen von %s %s um %s Uhr."
                   (:order/amount order)
                   (:product/name (:order/product order))
                   (to-time-str (:order/order-date order)))]
       [:p [:a {:href (path-for :home)} "zurück"]]]])})

(defn user-id [db {issuer :iss subject :sub name :name}]
  (if-let [{:keys [id]} (db/find-user db [:id] {:issuer issuer :subject subject})]
    (do
      (db/update-user! db id {:name name})
      id)
    (db/create-user! db {:name name :issuer issuer :subject subject})))

(defn oauth2-code-handler [{:keys [base-uri]}]
  (fn [{:keys [body db]}]
    (let [uri "https://www.googleapis.com/oauth2/v4/token"
          redirect-uri base-uri
          code (slurp body)
          resp @(http/post uri {:form-params {:grant_type "authorization_code"
                                              :code code
                                              :client_id (:client-id env)
                                              :client_secret (:client-secret env)
                                              :redirect_uri redirect-uri}
                                :throw-exceptions false})
          slurp-json (comp #(json/parse-string % keyword) slurp)]
      (if (= 200 (:status resp))
        (let [id-token (-> resp :body slurp-json :id_token)]
          (if-let [id-token (u/unsafe-unsign id-token)]
            {:status 200
             :body ""
             :session {:user-id (user-id db id-token)}}
            {:status 500
             :body "Invalid token..."
             :session {}}))
        (let [resp (update resp :body slurp-json)]
          (case (-> resp :body :error)
            "invalid_request" (println "Invalid request with code:" code "and redirect-uri:" redirect-uri)
            "redirect_uri_mismatch" (println "Wrong redirect uri:" redirect-uri))
          {:status 500
           :body ""
           :session {}})))))

(defn not-found-handler [req]
  {:status 404
   :body
   (html
     [:html
      (head (:path-for req) "Not Found" "1")
      [:body
       [:p "Oppps... Page not found."]]])})

(defn handlers [{:keys [db] :as env}]
  (assert db)
  {:home (-> home-handler
             (wrap-products db)
             (wrap-user db))
   :order (-> order-form-handler
              (wrap-product db)
              (wrap-auth)
              (wrap-user db))
   :order-confirmation (-> order-confirmation-handler
                           (wrap-user-order db [:order/amount {:order/product [:product/name]} :order/order-date])
                           (wrap-auth)
                           (wrap-user db))
   :post-order (-> order-post-handler
                   (wrap-store-order db)
                   (wrap-product db)
                   (wrap-auth)
                   (wrap-user db))
   :oauth2-code (oauth2-code-handler env)})
