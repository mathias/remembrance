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

(defn count-notes-q [db]
  (d/q '[:find (count ?e)
         :where [?e :note/guid _]]
       db))

(defn count-notes
  ([] (count-notes (db)))
  ([db]
     (-> (count-notes-q db)
         (ffirst)
         (or 0))))

(def allowed-note-keys-for-creation
  [:note/title
   :note/body
   :note/guid
   :note/articles])

(def allowed-note-keys-for-update
  [:note/title
   :note/body
   :note/articles])

(def note-keys-translations
  {:title    :note/title
   :guid     :note/guid
   :body     :note/body
   :articles :note/articles})

(defn translate-create-note-key-names [params]
  (-> params
      (clojure.set/rename-keys note-keys-translations)
      (select-keys allowed-note-keys-for-creation)))

(defn translate-update-note-key-names [params]
  (-> params
      (clojure.set/rename-keys note-keys-translations)
      (select-keys allowed-note-keys-for-update)))

(defn create-note-txn [conn attributes]
  (d/transact conn
              [(merge {:db/id (d/tempid "db.part/user")}
                      attributes)]))

(defn create-note
  ([params] (create-note remembrance.database/connection params))
  ([conn params]
     (let [translated-attrs (translate-create-note-key-names params)
           guid (new-guid)
           attrs (merge translated-attrs {:note/guid guid})]
       (create-note-txn conn attrs)
       (find-note-by-guid (d/db conn) guid))))

(defn update-note-txn [conn note attributes]
  (d/transact conn
              [(merge {:db/id (:db/id note)}
                      attributes)]))

(defn update-note [conn note params]
  (when-let [guid (:note/guid note)]
    (update-note-txn conn note (translate-update-note-key-names params))
    (find-note-by-guid (d/db conn) guid)))
