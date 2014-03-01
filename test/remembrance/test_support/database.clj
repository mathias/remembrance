(ns remembrance.test-support.database
  (:require [midje.sweet :refer :all]
            [datomic.api :as d]
            [remembrance.database :refer [prepare-database!]]
            [remembrance.config :refer [env]]))

(defn fresh-conn! []
  (let [uri (env :test-db-uri)]
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
