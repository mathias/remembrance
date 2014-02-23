(ns remembrance.models.note
  (:require [remembrance.config :refer [env]]
            [remembrance.models.article :refer [find-article-by-guid]]
            [datomic.api :as d]
            [remembrance.database :as database :refer [db new-guid t]]
            [taoensso.timbre :refer [info]]))

(defn entity [eid]
  (d/entity (db) eid))

(defn note-entity [entity-vec]
  (entity (first entity-vec)))

(defn find-all-note-ids-q [db]
  (d/q '[:find ?n
         :where [?n :note/guid _]]
       db))

(defn find-all-note-ids []
  (find-all-note-ids-q (db)))

(defn all-notes []
  (map note-entity (find-all-note-ids)))

(defn find-note-by-guid-q [db guid]
  (d/q '[:find ?eid
         :in $ ?guid
         :where [?eid :note/guid ?guid]]
       db
       guid))

(defn find-note-by-guid [guid]
  (find-note-by-guid-q (db) guid))

(defn find-articles-for-params [articles-param]
  (map ffirst (find-article-by-guid (db) (read-string articles-param))))

(defn create-note [{:keys [title body articles]
                    :or {title "Untitled"
                         body ""
                         articles "[]"}
                    :as all-params}]
  (let [db (db)
        guid (new-guid)]
    @(d/transact [{:db/id (d/tempid "db.part/user")
          :note/guid guid
          :note/title title
          :note/body body
            :note/articles (find-articles-for-params db articles)}]
          db)
    (find-note-by-guid-q db guid)))

(defn count-notes []
  (or (ffirst (d/q '[:find (count ?e)
                     :where [?e :note/guid _]]
                   (db)))
      0))

(defn show-note [guid]
  (let [results (find-note-by-guid guid)]
    (entity (ffirst results))))


(defn update-note [note attributes]
  (let [mapped-attributes (map (fn [[k v]] (condp = k
                                            :title {:note/title v}
                                            :body {:note/body v}
                                            :articles {:note/articles (find-articles-for-params v)}))
                               attributes)
        attributes-to-update (apply merge mapped-attributes)]
    @(t [(merge {:db/id (:db/id note)}
                attributes-to-update)])
    (find-note-by-guid (:note/guid note))))

(defn notes-stats []
  {:total (count-notes)})
