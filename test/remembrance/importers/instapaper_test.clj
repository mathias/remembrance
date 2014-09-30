(ns remembrance.importers.instapaper-test
  (:require [midje.sweet :refer :all]
            [clojure.test :refer :all]
            [org.httpkit.fake :refer :all]
            [remembrance.importers.instapaper :refer :all]))

(def example-instapaper-csv (slurp "test/remembrance/test_support/example-instapaper.csv"))

(deftest parse-csv-fn-tests
  (fact "Parses a simple Instapaper CSV into seq of expected maps"
    (parse-csv example-instapaper-csv) => (just [{:folder "Unread"
                                                   :selection ""
                                                   :title "Knapsack problem - Wikipedia, the free encyclopedia"
                                                   :url "http://en.wikipedia.org/wiki/Knapsack_problem"}])))

(deftest newspaper-url-fn-tests
  (fact "Appends the URL-encoded URL to newspaper-delivery URL"
    ;; note that test-env has newspaper-deliver-url set to"http://localhost:5000"
    (let [original-url "http://example.com"]
      (newspaper-url original-url) => "http://localhost:5000/article?url=http%253A%252F%252Fexample.com")))

(deftest import-article-fn-tests
  (let [response (slurp "test/remembrance/test_support/example-article.json")]
    (with-fake-http [#"^http://localhost:5000/article" response]
      (fact (import-article {:original_url "http://example.com"}) => response))))
