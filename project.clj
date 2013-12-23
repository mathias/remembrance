(defproject remembrance "0.1.1"
  :description "Note taking"
  :url "https://github.com/mathias/remembrance"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[cheshire "5.2.0"]
                 [clojure-csv "2.0.1"]
                 [com.cemerick/url "0.1.0"]
                 [com.datomic/datomic-pro "0.9.4324"]
                 [com.taoensso/carmine "2.4.0"]
                 [com.taoensso/timbre "2.7.1"]
                 [compojure "1.1.6"]
                 [hiccup "1.0.4"]
                 [http-kit "2.1.13"]
                 [org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-2080"]
                 [org.clojure/data.json "0.2.3"]
                 [prismatic/dommy "0.1.2"]
                 [ring/ring-json "0.2.0"]]

  ;; clojure source code pathname
  :source-paths ["src/clj"]

  :plugins [;; cljsbuild plugin
            [lein-cljsbuild "0.3.3"]

            ;; ring plugin
            [lein-ring "0.8.7"]]

  ;; datomic configuration
  :datomic {:schemas ["resources/schema" ["remem-schema.edn"
                                          "initial-data.edn"]]}
  :profiles {:dev
             {:datomic {:config "resources/sql-transactor-template.properties"
                        :db-uri "datomic:sql://remembrance?jdbc:postgresql://localhost:5432/datomic?user=datomic&password=datomic"}}}

  ;; ring tasks configuration
  :ring {:handler remembrance.core/remembrance-handler
         :init remembrance.core/remembrance-init
         :auto-refresh true
         :nrepl {:start? true}}

  ;; cljsbuild tasks configuration
  :cljsbuild {:builds
              [{;; clojurescript source code path
                :source-paths ["src/cljs"]

                ;; Google Closure Compiler options
                :compiler {;; the name of the emitted JS file
                           :output-to "resources/public/js/application.js"

                           ;; use minimal optimization CLS directive
                           :optimizations :whitespace

                           ;; prettyfying emitted JS
                           :pretty-print true}}]})
