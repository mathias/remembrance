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
   :article/original_url "http://example.com"})

(defn prepare-fresh-conn []
  (let [our-conn (fresh-conn!)]
    (prepare-database! our-conn)
    (d/transact our-conn [existing-article-txn])
    our-conn))

(fact "ensure our seed transaction works"
      (let [our-conn (prepare-fresh-conn)
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
              (let [our-conn (prepare-fresh-conn)
                    db (d/db our-conn)]
                 (find-article-by-guid-q db existing-guid))
              =not=>
              empty?)

       (fact  "found article has correct attributes"
              (let [our-conn (prepare-fresh-conn)
                    db (d/db our-conn)
                    eid (first (find-article-by-guid-q db existing-guid))]
                (:article/original_url (first-entity db eid)))
              =>
              "http://example.com")

       (fact "finding an entity that doesn't exist"
             (let [our-conn (prepare-fresh-conn)
                   db (d/db our-conn)]
               (find-article-by-guid-q db "made-up-guid"))
             =>
             empty?))
