(ns remembrance.database
  (:require [clojure.edn :as edn]
            [io.rkn.conformity :as c]
            [datomic.api :as d]
            [remembrance.config :as config]
            [taoensso.timbre :refer [info]]))

(def env (config/load!))

(def db-uri (env :db-uri))

(defonce connection (d/connect db-uri))

(defn db []
  (d/db connection))

(def t #(d/transact connection %))
(def simple-q #(d/q % (db)))

(defn new-guid []
  (str (d/squuid)))

;; migrations
(defn load-migration [filename]
  (read-string (slurp (str "resources/schema/" filename))))

(def migrations ["1387819157_add_articles.edn"
                 "1388418412_add_notes.edn"
                 "1388969538_add_ratings_to_articles.edn"])

(defn migrate! [migrations]
  (map (fn [migration-file]
         (let [migration (load-migration migration-file)
               migration-name (key (first migration))]
           (c/ensure-conforms connection migration (vec (list migration-name)))
           migration-name))
       migrations))

(defn prepare-database! []
  (migrate! migrations))

(defn shutdown-database! []
  ;; we send false to tell Datomic not to release all Clojure agents
  (d/shutdown false))
