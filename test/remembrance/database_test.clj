(ns remembrance.database-test
  (:require [midje.sweet :refer :all]
            [remembrance.config :refer [env]]
            [datomic.api :as d]
            [remembrance.database :refer :all]))

(defn fresh-conn! []
  (let [uri (env :test-db-uri)]
     (d/delete-database uri)
     (d/create-database uri)
     (d/connect uri)))

;; point queries to the in-mem test db
(background (around :facts (with-redefs [remembrance.database/connection (fresh-conn!)] ?form)))

(facts "testing fresh-conn!"
       (fact "should have no entities with a migration-created attribute"
             (d/q '[:find ?e
                    :where [?e :db/ident :article/guid]]
                  (d/db (fresh-conn!)))
             =>
             empty?))

(defn count-txes [db]
  (ffirst (d/q '[:find (count ?tx)
                 :where [?tx :db/txInstant _]]
               db)))

(facts "prepare-database!"
       ;; we don't need to inspect the migrations themselves since we assume
       ;; conformity is doing its job, so instead just check that some
       ;; transactions have happened since before the test
       (fact "runs all migrations on db"
             (let [our-conn (fresh-conn!)
                   before-txes-count (count-txes (d/db our-conn))]
               (prepare-database! our-conn)
               (> (count-txes (d/db our-conn)) before-txes-count))
             =>
             truthy))
