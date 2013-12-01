(ns remembrance.db
  (require [remembrance.config :as config]
           [datomic.api :as d]))

(def env (config/load!))

(def db-uri (env :db-uri))

(def connection (d/connect db-uri))

(defn db []
  (d/db connection))

(def q
  #(d/q % (db)))

(defn ent [id]
  (if-let [exists (ffirst (d/q '[:find ?eid :in $ ?eid :where [?eid]] (db) id))]
    (d/entity (db) exists)
    nil))

(defn eid [& conditions]
  (let [conditions (map #(concat ['?c] %) conditions)]
    (->
     {:find ['?c]
      :where conditions}
     q
     ffirst)))

(defn one [& conditions]
  (if-let [id (apply eid conditions)]
    (ent {:db/id id})))

(defn prepare! []
  (let [schema-tx (read-string (slurp "resources/schema/remem-schema.edn"))
        data-tx (read-string (slurp "resources/schema/initial-data.edn"))]
    ;; Since we're in dev, load the schema and seed data every time:
    @(d/transact connection schema-tx)
    @(d/transact connection data-tx)))

(def t #(d/transact connection %))
