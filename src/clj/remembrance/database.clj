(ns remembrance.database
  (:require [clojure.edn :as edn]
            [io.rkn.conformity :as c]
            [datomic.api :as d]
            [remembrance.config :as config]
            [taoensso.timbre :refer [info]]))

(def env (config/load!))

(def db-uri (env :db-uri))

(def connection (d/connect db-uri))

(defn db []
  (d/db connection))

(def t #(d/transact connection %))

;; migrations
(defn load-migration [filename]
  (read-string (slurp (str "resources/schema/" filename))))

(def migrations ["1387819157_add_articles.edn"])

(defn migrate! [migrations]
  (map (fn [migration-file]
         (let [migration (load-migration migration-file)
               migration-name (key (first migration))]
           (c/ensure-conforms connection migration (vec (list migration-name)))
           migration-name))
       migrations))

(defn prepare-database! []
  (migrate! migrations))
