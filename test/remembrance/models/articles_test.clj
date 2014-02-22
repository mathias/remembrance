(ns remembrance.models.articles_test
  (:require [midje.sweet :refer :all]
            [remembrance.models.article :refer :all]))

;; before-all
(def existing-article
  (entity (ffirst (find-article-by-original-url "http://example.com"))) )

(def existing-guid
  (:article/guid existing-article))

(def existing-articles-db-id
  (:db/id existing-article))

(def expected-article-attributes
  {:guid existing-guid
   :original_url "http://example.com"
   :read true})

(facts "attribute mapping"
       (fact "can map from datomic attribute to external attribute name"
             (translate-internal-attr-name :article/guid)
             =>
             :guid)
       (fact "can map from external attribute to internal datomic attribute"
             (translate-external-attr-name :url)
             =>
             :article/original_url))

(facts "finding one article"
       (fact "can find with a guid"
             (select-keys (find-one {:guid existing-guid}) [:title :url :read])
             =>
             expected-article-attributes))

(facts "building Datomic queries for any number of attributes"
       (fact "translating one article key and value"
             (article-attr-rules-from {:guid "asdf"})
             =>
             [['?eid :article/guid "asdf"]])
       (fact "can query for multiple attributes"
             (article-attr-rules-from {:guid "asdf"
                                       :read false})
             =>
             [['?eid :article/read false]
              ['?eid :article/guid "asdf"]])
       (fact "find-one-q can find all by an attr-map"
             (ffirst (find-q {:guid existing-guid}))
             =>
             existing-articles-db-id))
