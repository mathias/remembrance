(ns remembrance.models.note
  (:require [remembrance.config :as config]
            [datomic.api :as d]
            [remembrance.database :as database]
            [taoensso.timbre :refer [info]]))

(def env (config/load!))

(defn db [] (database/db))

(defn entity [eid]
  (d/entity (db) eid))

(defn note-entity [entity-vec]
  (entity (first entity-vec)))

(defn find-all-note-ids []
  (database/simple-q '[:find ?n
                       :where [?n :note/guid _]]))

(defn all-notes []
  (map note-entity (find-all-note-ids)))

(defn create-note [{:keys [title body articles]
                    :or {title "Untitled"
                         body ""
                         articles []}
                    :as all-params}]
  (let [guid (database/new-guid)]
    @(database/t [{:db/id (d/tempid "db.part/user")
                   :note/guid guid
                   :note/title title
                   :note/body body
                   :note/articles articles}])
    (entity (ffirst (d/q '[:find ?n
                           :in $ ?guid
                           :where [?n :note/guid ?guid]]
                          (db)
                          guid)))))
(defn count-notes []
  (or (ffirst (d/q '[:find (count ?e)
                     :where [?e :note/guid _]]
                   (db)))
      0))

(defn find-note-by-guid [guid]
  (d/q '[:find ?eid
         :in $ ?guid
         :where [?eid :note/guid ?guid]]
       (db)
       guid))

(defn show-note [guid]
  (let [results (find-note-by-guid guid)]
    (entity (ffirst results))))
