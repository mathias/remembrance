(ns remembrance.fetchers.newspaper-test
  (:require [midje.sweet :refer :all]
            [clojure.test :refer :all]
            [cemerick.url :refer [url]]
            [environ.core :refer [env]]
            [datomic.api :as d]
            [remembrance.test-support.database :refer :all]
            [remembrance.models.article :refer [find-article-by-guid]]
            [remembrance.fetchers.newspaper :refer :all]))

(deftest newspaper-url-fn-tests
  (fact "Appends the URL-encoded URL to newspaper-delivery URL"
    ;; note that test-env has newspaper-deliver-url set to"http://localhost:5000"
    (let [original-url "http://example.com"
          newspaper-delivery-url (env :newspaper-delivery-uri)]
      (newspaper-url original-url)
      =>
      (str newspaper-delivery-url "/article?url=http%3A%2F%2Fexample.com"))))

(let [response (slurp "test/remembrance/test_support/example-article.json")]
  (deftest update-article-with-fn-tests
    (let [our-conn (prepare-conn-with-seed-data)
          article (find-article-by-guid (d/db our-conn) existing-guid)
          ingest-time (java.util.Date.)
          opts {:db-conn our-conn
                :article article
                :ingest-time ingest-time}]
      (fact "updates the article"
        (-> (update-article-with response opts)
            :article/date_ingested)
        =>
        ingest-time))))
