(ns remembrance.acceptance.core-test
  (:require [midje.sweet :refer :all]
            [clojure.test :refer :all]
            [schema.core :as s]
            [remembrance.test-support.acceptance-helpers :refer :all]
            [remembrance.routes.response-schemas :refer :all]
            [remembrance.test-support.database :refer :all]))

(background
 (around :facts (with-redefs [remembrance.database/connection (prepare-conn-with-seed-data)] ?form)))

(deftest endpoints-are-all-accessible
  (fact "GET / (index-page)"
        (:body (get! "/")) => not-empty))
