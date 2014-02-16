(defproject remembrance "0.1.1"
  :description "Note taking"
  :url "https://github.com/mathias/remembrance"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[cheshire "5.2.0"]
                 [clojure-csv "2.0.1"]
                 [com.cemerick/url "0.1.0"]
                 [com.datomic/datomic-pro "0.9.4384"]
                 [com.taoensso/timbre "2.7.1"]
                 [playnice "1.0.1"]
                 [hiccup "1.0.4"]
                 [http-kit "2.1.13"]
                 [io.rkn/conformity "0.2.1" :exclusions [com.datomic/datomic-free]]
                 [liberator "0.10.0"]
                 [org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-2080"]
                 [org.clojure/data.json "0.2.3"]
                 [prismatic/dommy "0.1.2"]
                 [ring/ring-jetty-adapter "1.2.1"]
                 [ring/ring-json "0.2.0"]
                 [hearst "0.1.1-SNAPSHOT"]
                 [org.clojure/core.async "0.1.267.0-0d7780-alpha"]]

  ;; clojure source code pathname
  :source-paths ["src/clj"]

  :plugins [;; cljsbuild plugin
            [lein-cljsbuild "0.3.3"]

            ;; ring plugin
            [lein-ring "0.8.8"]]

  ;; datomic configuration
  :profiles {:dev
             {:datomic {:config "resources/sql-transactor-template.properties"
                        :db-uri "datomic:sql://remembrance?jdbc:postgresql://localhost:5432/datomic?user=datomic&password=datomic"}}}

  ;; ring tasks configuration
  :ring {:handler remembrance.core/remembrance-handler
         :init remembrance.core/remembrance-init
         :destroy remembrance.core/remembrance-clean-shutdown
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
