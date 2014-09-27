(ns remembrance.importers.instapaper-test
  (:require [midje.sweet :refer :all]
            [clojure.test :refer :all]
            [remembrance.importers.instapaper :refer :all]))

(def example-instapaper-csv (slurp "test/remembrance/test_support/example-instapaper.csv"))

(deftest parse-file-fn-tests
  (fact "Parses a simple Instapaper CSV into seq of expected maps"
    (parse-file example-instapaper-csv) => (just [{:folder "Unread"
                                                   :selection ""
                                                   :title "Knapsack problem - Wikipedia, the free encyclopedia"
                                                   :url "http://en.wikipedia.org/wiki/Knapsack_problem"}])))

