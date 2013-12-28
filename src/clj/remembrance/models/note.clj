(ns remembrance.models.note
  (:require [remembrance.config :refer [load!]]
            [datomic.api :as d]
            [remembrance.database :as database]))

(def env (load!))

(defn db [] (database/db))

(defn all-notes []
  (d/q '[:find ?n
         :where [?n :note/guid _]]
       (db)))
