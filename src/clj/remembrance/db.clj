(ns remembrance.db
  (require [remembrance.config :as config]
           [datomic.api :as d]))

(def env (config/load!))

(def db-uri (env :db-uri))

(def connection (d/connect db-uri))

(defn info []
  connection)

(defn prepare! []

  (def schema-tx (read-string (slurp "resources/schema/remem-schema.edn")))

  @(d/transact connection schema-tx))
