(ns remembrance.db
  (require [remembrance.config :as config]
           [datomic.api :as d]))

(def env (config/load!))

(def db-uri (env :db-uri))

(def connection (d/connect db-uri))

(defn db []
  (d/db connection))

(def t #(d/transact connection %))

(defn prepare! []
  (db))
