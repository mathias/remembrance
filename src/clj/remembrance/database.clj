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
(def migration-filenames ["1387819157_add_articles.edn"
                          "1388418412_add_notes.edn"
                          "1388969538_add_ratings_to_articles.edn"
                          "1392916852_add_newspaper_fields_to_articles.edn"])

(def migrations-path  "resources/schema/")

(def loaded-migrations (atom []))

(defn load-migration [filename]
  (-> (str migrations-path filename)
      (slurp)
      (read-string)))

(defn prepare-migration [migration]
  {:name (-> migration keys vec)
   :txn migration})

(defn load-all-migrations [filenames]
  (map load-migration filenames))

(defn migrate! [conn migration]
  (c/ensure-conforms conn (:txn migration) (:name migration)))

(defn run-each-migration! [conn prepared-migrations]
  (doseq [migration prepared-migrations]
    (migrate! conn (prepare-migration migration))))

(defn prepare-database!
  ([] (prepare-database! connection))
  ([conn]
     (reset! loaded-migrations (load-all-migrations migration-filenames))
     (run-each-migration! conn @loaded-migrations)))
