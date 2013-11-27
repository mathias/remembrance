(ns remembrance.db
  (require [remembrance.config :as config]
           [bitemyapp.revise.connection :refer [connect close]]
           [bitemyapp.revise.query :as r]
           [bitemyapp.revise.core :refer [run]]))

(def env (config/load!))
(def db-name (env :rethinkdb-db))
(defn connect-via-uri! []
  (connect {:host (env :rethinkdb-host)
            :port (env :rethinkdb-port)}))

(def rethinkdb-connection (connect-via-uri!))

(defmacro select [& exprs]
  `(->
     ~@(map (fn [expr] expr) exprs)
     (run rethinkdb-connection)))

(defn select-all [table-name]
  ((select
    (r/db db-name)
    (r/table-db table-name)) :response))

(defn prepare-db! []
  (let [db-list (first ((select (r/db-list)) :response))]
    (if-not (some #{db-name} db-list)
      (select
       (r/db-create (env :rethinkdb-db))))
    (select
     (r/db-list))))

(defn prepare-tables! []
  (let [table-list (first ((select (r/db db-name) (r/table-list-db)) :response))]
    (doseq [table-name (env :rethinkdb-tables)]
      (if-not (some #{table-name} table-list)
        (select
         (r/db db-name)
         (r/table-create-db table-name))))
    (select
     (r/db db-name)
     (r/table-list-db))))

(defn insert [table-name data]
  (select
   (r/db db-name)
   (r/table-db table-name)
   (r/insert data)))

(defn select-one [table-name id]
  (select
   (r/db db-name)
   (r/table-db table-name)
   (r/get id)))
