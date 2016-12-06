(ns takelist.handler
  "Here are all our handlers."
  (:require [clojure.pprint]
            [clojure.string :as str]
            [environ.core :refer [env]]
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
          url: 'http://localhost:8080/oauth2-code',
          contentType: 'application/octet-stream; charset=utf-8',
          success: function(result) {
            // Handle or verify the server response.
          },
          processData: false,
          data: authResult['code']
        });
      } else {
        // There was an error.
      }
    }
    " (:client-id env))]])

(defn home-handler [_]
  {:status 200
   :body
   (html
     [:html
      (head)
      [:body
       [:div {:class "container"}
        [:div {:class "row"}
         [:div {:class "col-xs-4 col-xs-offset-4"}
          [:button {:id "signinButton"} "Mit Google einloggen"]
          [:script
           "$('#signinButton').click(function() {
             // signInCallback defined in step 6.
              auth2.grantOfflineAccess({'redirect_uri': 'postmessage'}).then(signInCallback);
                });"]]]]]])})

(defn order-form-handler [{:keys [product user]}]
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
            [:p (format "Hiermit bestelle ich (%s) %s." (:name user) (:name product))]
            [:input {:type "hidden" :name "product-id" :value (:id product)}]]
           [:div {:class "form-group"}
            [:label {:for "amount"} "Anzahl"]
            [:select {:id "amount" :name "amount" :class "form-control"}
             (for [x (range 1 6)]
               [:option {:value x} x])]]
           [:button {:type "submit" :class "btn btn-primary"} "Ok"]]]]]]])})

(defn order-post-handler [{:keys [product params]}]
  {:status 200
   :body
   (html
     [:html
      (head)
      [:body
       [:p (let [{:keys [amount]} params]
             (format "Vielen Dank für das Bestellen von %s %s." amount (:name product)))]]])})

(defn oauth2-code-handler [{:keys [body]}]
  (println "code" (slurp body))
  {:status 200
   :body ""})

(defn not-found-handler [req]
  {:status 404
   :body
   (html
     [:html
      (head "Not Found" "1")
      [:body
       [:p "Oppps... Page not found."]]])})
