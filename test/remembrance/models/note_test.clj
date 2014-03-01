(ns remembrance.models.note-test
  (:require [midje.sweet :refer :all]
            [datomic.api :as d]
            [remembrance.test-support.database :refer :all]
            [remembrance.models.core :refer [first-entity]]
            [remembrance.models.note :refer :all]))

(fact "ensure our seed transaction works"
      (let [our-conn (prepare-conn-with-existing-note)
            db (d/db our-conn)
            txn-count (ffirst (d/q '[:find (count ?tx)
                                     :in $
                                     :where [?tx :db/txInstant _]]
                                   db))]
        (> txn-count 3))
      =>
      truthy)

(facts "find-note-by-guid-q"
       (facts "when note doesn't exist"
              (let [our-conn (prepare-conn-with-existing-note)
                    db (d/db our-conn)]
                (find-note-by-guid-q db "made-up-guid"))
              =>
              empty?)

       (facts "finding a note that exists"
              (fact "returns the set of found entity ID"
                    (let [our-conn (prepare-conn-with-existing-note)
                          db (d/db our-conn)]
                      (find-note-by-guid-q db existing-note-guid))
                    =not=>
                    empty?)

              (fact "found note has correct attributes when realized"
                    (let [our-conn (prepare-conn-with-existing-note)
                          db (d/db our-conn)]
                      (->> existing-note-guid
                           (find-note-by-guid-q db)
                           (first)
                           (first-entity db)
                           (:note/title)))
                    =>
                    "Example title")))

(facts "find-note-by-guid fn"
       (fact "turns the first result entity ID into an entity"
              (let [our-conn (prepare-conn-with-existing-note)
                   db (d/db our-conn)
                   note (find-note-by-guid db existing-note-guid)]
               (:note/title note))
             =>
             "Example title"))

(facts "search-notes-q fn"
       (fact "finds a note which matches the search query"
             (let [our-conn (prepare-conn-with-existing-note)
                   db (d/db our-conn)]
               (search-notes-q db "Example"))
             =not=>
             empty?)

       (fact "does not find any when query does not match"
             (let [our-conn (prepare-conn-with-existing-note)
                   db (d/db our-conn)]
               (search-notes-q db "asdf"))
             =>
             empty?))

(facts "search-notes fn"
       (fact "turns returned list of entity IDs into entities (can get attributes)"
             (let [our-conn (prepare-conn-with-existing-note)
                   db (d/db our-conn)
                   query "Example"]
               (->> query
                    (search-notes db)
                    (first)
                    (:note/title)))
             =>
             "Example title"))

(facts "find-all-notes-q fn"
       (facts "when no notes exist"
              (facts "returns empty set"
                     (let [our-conn (prepare-migrated-db-conn)
                           db (d/db our-conn)]
                       (find-all-notes-q db))
                     =>
                     empty?))

       (facts "when note exists"
              (fact "returns the set with entity ID in it"
                     (let [our-conn (prepare-conn-with-existing-note)
                           db (d/db our-conn)]
                       (find-all-notes-q db))
                     =not=>
                     empty?)))

(facts "find-all-notes fn"
       (fact "maps entity IDs to entities (can get attributes)"
             (let [our-conn (prepare-conn-with-existing-note)
                   db (d/db our-conn)]
               (-> (find-all-notes db)
                   (first)
                   (:note/title)))
             =>
             "Example title"))

(facts "count-notes-q fn"
       (fact "returns empty set when no notes exist"
             (let [our-conn (prepare-migrated-db-conn)
                   db (d/db our-conn)]
               (count-notes-q db))
             =>
             empty?)

       (fact "when note exists, returns the set with the count in it"
             (let [our-conn (prepare-conn-with-existing-note)
                   db (d/db our-conn)]
               (-> (count-notes-q db)
                   (ffirst)))
             =>
             1))

(facts "create-note fn"
       (fact "can successfully create a note with given attributes"
             (let [our-conn (prepare-migrated-db-conn)
                   db (d/db our-conn)]
               (->> {:title "Our note title" :body "Our body"}
                    (create-note our-conn)
                    (:note/title)))
             =>
             "Our note title")

       (fact "are unique so multiple can be created with some title/body"))

(facts "translate-create-note-key-names fn"
       (fact "translates a key it knows about"
             (translate-create-note-key-names {:title "foo"})
             =>
             {:note/title "foo"})

       (fact "it filters keys that are not in the list (sanitizes params"
             (translate-create-note-key-names {:title "foo" :other "bar"})
             =>
             {:note/title "foo"}))

(facts "update-note fn"
       (facts "when note does not exist"
              (fact "returns falsey but does not raise error"
                    (let [our-conn (prepare-migrated-db-conn)
                          note nil
                          params {:body "Updated body"}]
                      (update-note our-conn note params))
                    =>
                    falsey))
       (facts "when the note exists"
              (fact "it updates the attribute specified"
                    (let [our-conn (prepare-conn-with-existing-note)
                          note (find-note-by-guid (d/db our-conn) existing-note-guid)
                          params {:body "Updated body"}]
                      (->> params
                           (update-note our-conn note)
                           (:note/body)))
                    =>
                    "Updated body")))


(facts "translate-update-note-key-names fn"
       (fact "translates a key it knows about"
             (translate-update-note-key-names {:title "foo"})
             =>
             {:note/title "foo"})

       (fact "it filters keys that are not in the list (sanitizes params"
             (translate-update-note-key-names {:title "foo" :other "bar"})
             =>
             {:note/title "foo"})

       (fact "does not allow changing things like guid in update"
             (translate-update-note-key-names {:title "foo" :guid "new-guid"})
             =>
             {:note/title "foo"}))
