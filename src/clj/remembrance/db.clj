(ns remembrance.db
  (require [remembrance.config :as config]
           [datomic.api :as d]))

(def env (config/load!))

(def db-uri (env :db-uri))

(def connection (d/connect db-uri))

(defn db []
  (d/db connection))

(defn prepare! []
  (let [schema-tx (read-string (slurp "resources/schema/remem-schema.edn"))
        data-tx (read-string (slurp "resources/schema/initial-data.edn"))]
    ;; Since we're in dev, load the schema and seed data every time:
    @(d/transact connection schema-tx)
    @(d/transact connection data-tx)))

(def t #(d/transact connection %))
