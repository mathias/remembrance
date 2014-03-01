(ns remembrance.models.note
  (:require [remembrance.config :refer [env]]
            [remembrance.models.core :refer [first-entity]]
            [remembrance.models.article :refer [find-article-by-guid-q]]
            [datomic.api :as d]
            [remembrance.database :as database :refer [db new-guid]]
            [taoensso.timbre :refer [info]]))

(defn find-note-by-guid-q [db guid]
  (d/q '[:find ?eid
         :in $ ?guid
         :where [?eid :note/guid ?guid]]
       db
       guid))

(defn find-note-by-guid
  ([guid] (find-note-by-guid (db) guid))
  ([db guid]
     (->> guid
          (find-note-by-guid-q db)
          (first)
          (first-entity db))))

(defn search-notes-q [db query-string]
  (d/q '[:find ?e
         :in $ % ?query
         :where (search-rules ?query ?e)]
       db
       '[[(search-rules ?query ?e)
          [(fulltext $ :note/title ?query) [[?e]]]]
         [(search-rules ?query ?e)
          [(fulltext $ :note/body ?query) [[?e]]]]]
       query-string))

(defn search-notes
  ([query-string] (search-notes (db) query-string))
  ([db query-string]
     (let [results (search-notes-q db query-string)]
       (map (partial first-entity db) results))))

(defn find-all-notes-q [db]
  (d/q '[:find ?e
         :where [?e :note/title _]]
       db))

(defn find-all-notes
  ([] (find-all-notes (db)))
  ([db]
     (let [note-entities (find-all-notes-q db)]
       (map (partial first-entity db) note-entities))))

;; (defn find-articles-for-params-q [db article-guids]
;;   (map (partial find-article-by-guid-q db) article-guids))

;; (defn find-articles-for-params
;;   [db articles-param]
;;      (find-articles-for-params-q db (read-string articles-param)))

;; (defn create-note [{:keys [title body articles]
;;                     :or {title "Untitled"
;;                          body ""
;;                          articles "[]"}
;;                     :as all-params}]
;;   (let [conn remembrance.database/connection
;;         db (d/db conn)
;;         guid (new-guid)]
;;     (d/transact conn
;;                 [{:db/id (d/tempid "db.part/user")
;;                   :note/guid guid
;;                   :note/title title
;;                   :note/body body
;;                   :note/articles (find-articles-for-params db articles)}])
;;     (find-note-by-guid db guid)))

;; (defn count-notes []
;;   (or (ffirst (d/q '[:find (count ?e)
;;                      :where [?e :note/guid _]]
;;                    (db)))
;;       0))

;; (defn show-note [guid]
;;   (let [results (find-note-by-guid guid)]
;;     (entity (ffirst results))))


;; (defn update-note [note attributes]
;;   (let [mapped-attributes (map (fn [[k v]] (condp = k
;;                                             :title {:note/title v}
;;                                             :body {:note/body v}
;;                                             :articles {:note/articles (find-articles-for-params v)}))
;;                                attributes)
;;         attributes-to-update (apply merge mapped-attributes)]
;;     @(d/transact remembrance.database/connection
;;                  [(merge {:db/id (:db/id note)}
;;                          attributes-to-update)])
;;     (find-note-by-guid (:note/guid note))))

;; (defn notes-stats []
;;   {:total (count-notes)})
