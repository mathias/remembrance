(defproject remembrance "0.2.0-SNAPSHOT"
  :description "Note taking"
  :url "https://github.com/mathias/remembrance"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[cheshire "5.3.1"]
                 [clojure-csv "2.0.1"]
                 [com.cemerick/friend "0.2.1"]
                 [hiccup "1.0.5"]
                 [liberator "0.12.2"]
                 [org.clojure/clojure "1.6.0"]
                 [playnice "1.0.1"]
                 [ring/ring-jetty-adapter "1.3.1"]
                 [ring/ring-json "0.3.1"]]

  :source-paths ["src/clj"]

  :plugins [;; cljsbuild plugin
            [lein-cloverage "1.0.2"]

            ;; ring plugin
            [lein-ring "0.8.11"]]

  :profiles {:dev
             {:dependencies [[midje "1.6.3"]
                             [ring-mock "0.1.5"]]
              :plugins [[lein-midje "3.1.3"]]}}

  :ring {:handler remembrance.core/remembrance-handler
         :init remembrance.core/remembrance-init
         :auto-refresh true
         :nrepl {:start? true}})
