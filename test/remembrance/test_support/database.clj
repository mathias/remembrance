(ns remembrance.test-support.database
  (:require [midje.sweet :refer :all]
            [datomic.api :as d]
            [remembrance.database :refer [prepare-database!]]
            [environ.core :refer [env]]))

(defn fresh-conn! []
  (let [uri (env :database-uri)]
     (d/delete-database uri)
     (d/create-database uri)
     (d/connect uri)))

(defn prepare-migrated-db-conn []
  (let [our-conn (fresh-conn!)]
    (prepare-database! our-conn)
    our-conn))

(def original-url "http://example.com")
(def existing-guid "existing-guid")

(def existing-article-txn
  {:db/id (d/tempid "db.part/user")
   :article/guid existing-guid
   :article/original_url original-url
   :article/title "Example"
   :article/read false
   :article/ingest_state :article.ingest_state/ingested})

(def existing-read-article-txn
  (merge existing-article-txn
         {:article/read true}))

(defn prepare-conn-with-existing-article []
  (let [our-conn (prepare-migrated-db-conn)]
    (d/transact our-conn [existing-article-txn])
    our-conn))

(defn prepare-conn-with-read-article []
  (let [our-conn (prepare-migrated-db-conn)]
    (d/transact our-conn [existing-read-article-txn])
    our-conn))

(def existing-note-guid "existing-note-guid")

(def existing-note-txn
  {:db/id (d/tempid "db.part/user")
   :note/guid existing-note-guid
   :note/title "Example title"
   :note/body "Example body"
   :note/articles []})

(def empty-note-guid "empty-note-guid")

(def empty-note-txn
  {:db/id (d/tempid "db.part/user")
   :note/guid empty-note-guid})

(defn prepare-conn-with-existing-note []
  (let [our-conn (prepare-migrated-db-conn)]
    (d/transact our-conn [existing-note-txn])
    our-conn))

(defn prepare-conn-with-seed-data []
  (let [our-conn (prepare-migrated-db-conn)]
    (d/transact our-conn [existing-article-txn
                          existing-note-txn
                          empty-note-txn])
    our-conn))
