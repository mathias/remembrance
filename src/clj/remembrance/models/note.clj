(ns remembrance.models.note
  (:require [remembrance.config :refer [load!]]
            [datomic.api :as d]
            [remembrance.database :as database]
            [taoensso.timbre :refer [info]]))

(def env (load!))

(defn db [] (database/db))

(defn entity [eid]
  (d/entity (db) eid))

(defn note-entity [entity-vec]
  (entity (first entity-vec)))

(defn find-all-note-ids []
  (d/q '[:find ?n
         :where [?n :note/guid _]]
       (db)))

(defn all-notes []
  (map note-entity (find-all-note-ids)))

(defn create-note [{:keys [title body articles]
                    :or {title "Untitled"
                         body ""
                         articles []}
                    :as all-params}]
  (info all-params)
  @(database/t [{:db/id (d/tempid "db.part/user")
                :note/guid (database/new-guid)
                :note/title title
                :note/body body
                :note/articles articles}]))
