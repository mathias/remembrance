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
        db-list ((select (r/db-list)) :response)]
    (if-not (contains? (set db-list) db-name)
      (select
       (r/db-create (env :rethinkdb-db))))))

(defn prepare-tables! []
  (let [db-name (env :rethinkdb-db)
        table-list ((select (r/db db-name) (r/table-list-db)) :response)]
    (if-not (contains? (set table-list) "documents")
      (select
       (r/db db-name)
       (r/table-create-db "documents")))))
