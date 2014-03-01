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

(def existing-guid "existing-guid")

(def existing-article-txn
  {:db/id (d/tempid "db.part/user")
   :article/guid existing-guid
   :article/original_url "http://example.com"
   :article/title "Example"
   :article/read true
   :article/ingest_state :article.ingest_state/ingested})


(defn prepare-conn-with-existing-article []
  (let [our-conn (prepare-migrated-db-conn)]
    (d/transact our-conn [existing-article-txn])
    our-conn))
