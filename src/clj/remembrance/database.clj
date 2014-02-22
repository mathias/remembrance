(ns remembrance.database
  (:require [clojure.edn :as edn]
            [io.rkn.conformity :as c]
            [datomic.api :as d]
            [remembrance.config :refer [env]]
            [taoensso.timbre :refer [info]]))

(def db-uri (env :db-uri))

(def connection (d/connect db-uri))

(defn db []
  (d/db connection))

(def t #(d/transact connection %))

(defn new-guid []
  (str (d/squuid)))

;; migrations
(def migrations ["1387819157_add_articles.edn"
                 "1388418412_add_notes.edn"
                 "1388969538_add_ratings_to_articles.edn"
                 "1392916852_add_newspaper_fields_to_articles.edn"])

(defn load-migration! [filename]
  (let [migration (read-string (slurp (str "resources/schema/" filename)))
        migration-name (vec (keys migration))]
    {:name migration-name
     :txn migration}))

(defn loaded-migrations []
  (map load-migration! migrations))

(defn migrate! [conn loaded-migrations]
  (doseq [migration loaded-migrations]
    (c/ensure-conforms conn (:txn migration) (:name migration))))

(defn prepare-database!
  ([] (prepare-database! connection))
  ([conn]
     (migrate! conn (loaded-migrations))))
