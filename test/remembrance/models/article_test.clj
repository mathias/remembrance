(ns remembrance.models.article-test
  (:require [midje.sweet :refer :all]
            [datomic.api :as d]
            [remembrance.database :refer [prepare-database!]]
            [remembrance.test-support.database :refer :all]
            [remembrance.models.core :refer [first-entity]]
            [remembrance.models.article :refer :all]))



(fact "ensure our seed transaction works"
      (let [our-conn (prepare-conn-with-existing-article)
            db (d/db our-conn)
            txn-count (ffirst (d/q '[:find (count ?tx)
                                     :in $
                                     :where [?tx :db/txInstant _]]
                                   db))]
        (> txn-count 3))
      =>
      truthy)

(facts "find-article-by-guid-q fn"
       (fact "finds an existing article"
             (let [our-conn (prepare-conn-with-existing-article)
                   db (d/db our-conn)]
               (find-article-by-guid-q db existing-guid))
             =not=>
             empty?)

       (fact "found article has correct attributes"
             (let [our-conn (prepare-conn-with-existing-article)
                   db (d/db our-conn)
                   eid (first (find-article-by-guid-q db existing-guid))]
               (:article/original_url (first-entity db eid)))
             =>
             original-url)

       (fact "finding an entity that doesn't exist"
             (let [our-conn (prepare-conn-with-existing-article)
                   db (d/db our-conn)]
               (find-article-by-guid-q db "made-up-guid"))
             =>
             empty?))

(facts "find-article-by-guid fn"
       (fact "turns the first result entity ID into an entity"
              (let [our-conn (prepare-conn-with-existing-article)
                   db (d/db our-conn)
                   article (find-article-by-guid db existing-guid)]
               (:article/original_url article))
             =>
             original-url))

(facts "search-articles-q fn"
       (fact "finds an article which matches the search query"
             (let [our-conn (prepare-conn-with-existing-article)
                   db (d/db our-conn)]
               (search-articles-q db "Example"))
             =not=>
             empty?)

       (fact "article that does not match query is not found"
             (let [our-conn (prepare-conn-with-existing-article)
                   db (d/db our-conn)]
               (search-articles-q db "Not matching"))
             =>
             empty?))

(facts "search-articles fn"
       (fact "maps returned list of entity ids into entities (can get attributes)"
             (let [our-conn (prepare-conn-with-existing-article)
                   db (d/db our-conn)]
               (:article/original_url (first (search-articles db "Example"))))
             =>
             original-url))

(facts "find-all-ingested-articles-q fn"
       (fact "returns a set containing our existing article"
             (let [our-conn (prepare-conn-with-existing-article)
                   db (d/db our-conn)]
                (find-all-ingested-articles-q db))
             =not=>
             empty?)

       (fact "found article has correct attributes"
             (let [our-conn (prepare-conn-with-existing-article)
                   db (d/db our-conn)
                   found-eids (find-all-ingested-articles-q db)
                   first-found-eid (first found-eids)]
               (:article/original_url (first-entity db first-found-eid)))
             =>
             original-url)

       (fact "when no ingested articles exist, returns empty set"
             (let [our-conn (prepare-migrated-db-conn)
                   db (d/db our-conn)]
                (find-all-ingested-articles-q db))
             =>
             empty?))

(facts "find-all-ingested-articles"
       (fact "returns entities for ingested articles"
             (let [our-conn (prepare-conn-with-existing-article)
                   db (d/db our-conn)]
               (-> (find-all-ingested-articles db)
                   (first)
                   (:article/original_url)))
             =>
             original-url))

(facts "count-read-articles-q fn"
       (fact "returns empty set when no read articles exist"
             (let [our-conn (prepare-migrated-db-conn)
                   db (d/db our-conn)]
                (count-read-articles-q db))
             =>
             empty?)

       (fact "returns the correct count (1) when a single read article exists"
             (let [our-conn (prepare-conn-with-existing-article)
                   db (d/db our-conn)]
               (count-read-articles-q db))
             =>
             1))

(facts "count-read-articles"
       (fact "returns 0 when there are none"
             (let [our-conn (prepare-migrated-db-conn)
                   db (d/db our-conn)]
               (count-read-articles db))
             =>
             0)

       (fact "returns the count when one read article exists"
             (let [our-conn (prepare-conn-with-existing-article)
                   db (d/db our-conn)]
               (count-read-articles db))
             =>
             1))

(facts "count-all-articles-q fn"
       (fact "returns an empty set when there are none"
             (let [our-conn (prepare-migrated-db-conn)
                   db (d/db our-conn)]
               (count-all-articles-q db))
             =>
             empty?)

       (fact "returns the count when one read article exists"
             (let [our-conn (prepare-conn-with-existing-article)
                   db (d/db our-conn)]
               (count-all-articles-q db))
             =>
             1))

(facts "count-all-articles fn"
        (fact "returns 0 when there are none"
             (let [our-conn (prepare-migrated-db-conn)
                   db (d/db our-conn)]
               (count-all-articles db))
             =>
             0)

       (fact "returns the count when one read article exists"
             (let [our-conn (prepare-conn-with-existing-article)
                   db (d/db our-conn)]
               (count-all-articles db))
             =>
             1))

(facts "count-articles-with-ingest-state-q fn"
       (fact "takes an ingest state and returns the count of how many there are"
             (let [our-conn (prepare-conn-with-existing-article)
                   db (d/db our-conn)]
               (count-articles-with-ingest-state-q db :article.ingest_state/ingested))
             =>
             1)

       (fact "finds empty set for an ingest state with no articles"
             (let [our-conn (prepare-conn-with-existing-article)
                   db (d/db our-conn)]
               (count-articles-with-ingest-state-q db :article.ingest_state/fetched))
             =>
             empty?))

(facts "count-all-articles-with-ingest-state fn"
       (fact "returns 0 when there are no articles of given state"
             (let [our-conn (prepare-conn-with-existing-article)
                   db (d/db our-conn)]
               (count-articles-with-ingest-state db :article.ingest_state/fetched))
             =>
             0)

       (fact "returns the count when articles exist with ingest stae"
             (let [our-conn (prepare-conn-with-existing-article)
                   db (d/db our-conn)]
               (count-articles-with-ingest-state db :article.ingest_state/ingested))
             =>
             1))

(facts "create-article"
       (facts "when no article exists"
              (fact "can create an article successfully with original_url"
                    (let [our-conn (prepare-migrated-db-conn)]
                      (-> (create-article our-conn {:original_url original-url})
                          (:article/original_url)))
                    =>
                    original-url))
       (facts "when an article already exists with the same original-url"
              (fact "it returns the existing entity rather than creating one"
                    (let [our-conn (prepare-conn-with-existing-article)]
                      ;; we know that our transaction for existing article
                      ;; gives us a guid of existing-guid
                      ;; so we check for that here
                      (-> (create-article our-conn {:original_url original-url})
                          (:article/guid)))
                    =>
                    existing-guid)))
