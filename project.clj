(defproject takelist "0.1-SNAPSHOT"
  :description "An application which manages things like coffee or drinks which are available in a shared space at work."
  :url "https://github.com/alexanderkiel/takelist"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies
  [
   ;; Webserver
   [aleph "0.4.1"]

   ;; HTML Output
   [hiccup "1.0.5"]

   ;; Clojure itself
   [org.clojure/clojure "1.8.0"]

   ;; Web middleware and handler specification
   [ring/ring-core "1.5.0"]]

  :profiles {:dev
             {:source-paths ["dev"]
              :dependencies [[org.clojure/tools.namespace "0.2.11"]]}

             :production
             {:main takelist.core}})
