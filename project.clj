(defproject remembrance "0.1.0-SNAPSHOT"
  :description "Note taking"
  :url "https://github.com/mathias/remembrance"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-2014"]
                 [compojure "1.1.6"]
                 [hiccup "1.0.4"]
                 [prismatic/dommy "0.1.2"]]

  ;; clojure source code pathname
  :source-paths ["src/clj"]


  :plugins [;; cljsbuild plugin
            [lein-cljsbuild "0.3.3"]

            ;; ring plugin
            [lein-ring "0.8.7"]]

  ;; ring tasks configuration
  :ring {:handler remembrance.core/handler}

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
