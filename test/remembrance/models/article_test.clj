(ns remembrance.models.article-test
  (:require [midje.sweet :refer :all]
            [datomic.api :as d]
            [remembrance.database :refer [prepare-database!]]
            [remembrance.database-test :refer [fresh-conn!]]
            [remembrance.models.core :refer [first-entity]]
            [remembrance.models.article :refer :all]))

(def existing-guid "existing-guid")

(def existing-article-txn
  {:db/id (d/tempid "db.part/user")
   :article/guid existing-guid
   :article/original_url "http://example.com"
   :article/title "Example"
   :article/read true
   :article/ingest_state :article.ingest_state/ingested})

(defn prepare-conn-with-existing-article []
  (let [our-conn (fresh-conn!)]
    (prepare-database! our-conn)
    (d/transact our-conn [existing-article-txn])
    our-conn))

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
             "http://example.com")

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
             "http://example.com"))

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
             "http://example.com"))

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
             "http://example.com")

       (fact "when no ingested articles exist, returns empty set"
             (let [our-conn (fresh-conn!)
                   _ (prepare-database! our-conn)
                   db (d/db our-conn)]
                (find-all-ingested-articles-q db))
             =>
             empty?))

(facts "find-all-ingested-articles"
       (fact "returns entities for ingested articles"
             (let [our-conn (prepare-conn-with-existing-article)
                   db (d/db our-conn)
                   articles (find-all-ingested-articles db)
                   first-found-article (first articles)]
               (:article/original_url first-found-article))
             =>
             "http://example.com"))

(facts "count-read-articles-q fn"
       (fact "returns empty set when no read articles exist"
             (let [our-conn (fresh-conn!)
                   _ (prepare-database! our-conn)
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
             (let [our-conn (fresh-conn!)
                   _ (prepare-database! our-conn)
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
