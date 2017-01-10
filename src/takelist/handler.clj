(ns takelist.handler
  "Here are all our handlers."
  (:require [aleph.http :as http]
            [buddy.sign.jwt :as jwt]
            [cheshire.core :as json]
            [clojure.java.jdbc :as j]
            [clojure.pprint :refer [pprint]]
            [clojure.string :as str]
            [environ.core :refer [env]]
            [hiccup.core :refer [html]]
            [takelist.db :as db]
            [takelist.middleware.auth :refer [wrap-auth]]
            [takelist.middleware.product :refer [wrap-product]]
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

(defn home-handler [{:keys [user path-for]}]
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
            [:p "Produktliste"]
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

(defn order-post-handler [{:keys [product params path-for]}]
  {:status 200
   :body
   (html
     [:html
      (head path-for)
      [:body
       [:p (let [{:keys [amount]} params]
             (format "Vielen Dank für das Bestellen von %s %s." amount (:name product)))]]])})

(defn user-id [db {issuer :iss subject :sub given-name :given_name}]
  (if-let [{:keys [id]} (db/find-user db [:id] {:issuer issuer :subject subject})]
    (do
      (db/update-user! db id {:name given-name})
      id)
    (db/create-user! db {:name given-name :issuer issuer :subject subject})))

(defn oauth2-code-handler [{:keys [base-uri]}]
  (fn [{:keys [body db]}]
    (let [uri "https://www.googleapis.com/oauth2/v4/token"
          redirect-uri base-uri
          resp @(http/post uri {:form-params {:grant_type "authorization_code"
                                              :code (slurp body)
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
  {:home (wrap-user home-handler db)
   :order (-> order-form-handler
              (wrap-product db)
              (wrap-auth)
              (wrap-user db))
   :post-order (-> order-post-handler
                   (wrap-product db)
                   (wrap-auth)
                   (wrap-user db))
   :oauth2-code (oauth2-code-handler env)})
