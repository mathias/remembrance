(ns remembrance.importers.instapaper-test
  (:require [midje.sweet :refer :all]
            [clojure.test :refer :all]
            [datomic.api :as d]
            [org.httpkit.client :as http]
            [org.httpkit.fake :refer :all]
            [environ.core :refer [env]]
            [cemerick.url :refer [url]]
            [remembrance.test-support.database :refer :all]
            [remembrance.importers.instapaper :refer :all]
            [remembrance.fetchers.newspaper :refer :all]
            [remembrance.models.article :refer [find-article-by-guid]]))

(def example-instapaper-csv (slurp "test/remembrance/test_support/example-instapaper.csv"))

(deftest parse-csv-fn-tests
  (fact "Parses a simple Instapaper CSV into seq of expected maps"
    (parse-csv example-instapaper-csv)
    =>
    (just [{:folder "Unread"
            :selection ""
            :title "Knapsack problem - Wikipedia, the free encyclopedia"
            :url "http://en.wikipedia.org/wiki/Knapsack_problem"}])))

;; All tests that might try to call out through http-kit need to be inside this
(let [response (slurp "test/remembrance/test_support/example-article.json")
      newspaper-delivery (assoc (url (env :newspaper-delivery-uri)) :path "/article")]
  (with-fake-http [(re-pattern (str "^" newspaper-delivery)) response]
    (facts "when new articles are created"
      (let [our-conn (prepare-migrated-db-conn)
            article-data {:url "http://example.com" :title "Example"}]
        (fact "returns the newly-created article"
          (-> (create-and-setup-article our-conn article-data)
              (:article/original_url))
          =>
          "http://example.com")))))
