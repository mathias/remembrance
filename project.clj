(defproject remembrance "0.1.1"
  :description "Where is my mind?"
  :url "https://github.com/mathias/remembrance"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[cheshire "5.3.1"]
                 [clojure-csv "2.0.1"]
                 [com.cemerick/url "0.1.1"]
                 [com.datomic/datomic-pro "0.9.4384"]
                 [com.taoensso/timbre "3.3.1"]
                 [environ "1.0.0"]
                 [hearst "0.1.2"]
                 [http-kit "2.1.19"]
                 [io.rkn/conformity "0.3.2" :exclusions [com.datomic/datomic-free]]
                 [liberator "0.12.2"]
                 [org.clojure/clojure "1.6.0"]
                 [org.clojure/core.async "0.1.267.0-0d7780-alpha"]
                 [playnice "1.0.1"]
                 [prismatic/schema "0.3.0"]
                 [ring/ring-jetty-adapter "1.3.1"]
                 [ring/ring-json "0.3.1"]
                 [midje "1.6.3"]
                 [ring-mock "0.1.5"]
                 [http-kit.fake "0.2.1"]]

  :plugins [[lein-cloverage "1.0.2"]
            [lein-environ "1.0.0"]
            [lein-midje "3.1.3"]
            [lein-ring "0.8.11"]]

  :profiles {:dev
             {:datomic {:config "resources/sql-transactor-template.properties"
                        :db-uri "datomic:sql://remembrance?jdbc:postgresql://localhost:5432/datomic?user=datomic&password=datomic"}
              :env {:database-uri "datomic:sql://remembrance?jdbc:postgresql://localhost:5432/datomic?user=datomic&password=datomic"
                    :hostname "http://remembrance.local:3000"
                    :newspaper-delivery-uri "http://localhost:5000"}}
             :test {:env {:database-uri "datomic:mem://test"
                          :hostname "http://remembrance.local:3000"
                          :newspaper-delivery-uri "http://testing:5000"}}}

  :ring {:handler remembrance.core/remembrance-handler
         :init remembrance.core/remembrance-init
         :auto-refresh true
         :nrepl {:start? true}})
