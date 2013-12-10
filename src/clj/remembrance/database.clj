(ns remembrance.database
  (:require [datomic.api :as d]
            [remembrance.config :as config]))

(def env (config/load!))

(def db-uri (env :db-uri))

(def connection (d/connect db-uri))

(defn db []
  (d/db connection))

(def t #(d/transact connection %))

(defn prepare! []
  (db))
