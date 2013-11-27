(ns remembrance.db
  (require [remembrance.config :as config]
           [bitemyapp.revise.connection :refer [connect close]]
           [bitemyapp.revise.query :as r]
           [bitemyapp.revise.core :refer [run]]))

(def env (config/load!))

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
    (r/db (env :rethinkdb-db))
    (r/table-db table-name)) :response))

(defn prepare-db! []
  (let [db-name (env :rethinkdb-db)
        db-list (first ((select (r/db-list)) :response))]
    (if-not (some #{db-name} db-list)
      (select
       (r/db-create (env :rethinkdb-db))))
    (select
     (r/db-list))))

(defn prepare-tables! []
  (let [db-name (env :rethinkdb-db)
        table-list (first ((select (r/db db-name) (r/table-list-db)) :response))]
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
   (r/db (env :rethinkdb-db))
   (r/table-db table-name)
   (r/insert data)))
