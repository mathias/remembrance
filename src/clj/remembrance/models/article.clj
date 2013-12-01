(ns remembrance.models.article
  (require [remembrance.db :as db]
           [remembrance.workers :refer [enqueue-article-ingest]]
           [datomic.api :as d]
           [taoensso.timbre :refer [info]]))

(defn find-article-by-guid [guid]
  (d/q '[:find ?eid
         :in $ ?guid
         :where [?eid :article/guid ?guid]]
       (db/db)
       guid))

(defn show-article [guid]
  (let [results (find-article-by-guid guid)
        eid (ffirst results)]
    (d/entity (db/db) eid)))

(defn find-article-by-original-url [original-url]
  (d/q '[:find ?e :
         in $ ?original_url
         :where [?e :article/original_url ?original_url]]
       (db/db)
       original-url))

(defn create-article [attrs]
  ;; try to find an existing one first
  (let [original-url (attrs :original_url)
        existing (find-article-by-original-url original-url)]
    (if (empty? existing)
      (let [guid (str (d/squuid))]
        ;; create the new article:
        @(db/t [{:db/id (d/tempid "db.part/user")
                 :article/guid guid
                 :article/original_url original-url
                 :article/ingest_state "new"}])
        (enqueue-article-ingest guid)
        ;; redirect to article
        guid)
      ((first existing) :guid))))

(defn find-all-article-ids []
  (d/q '[:find ?a
         :where [?a :article/guid]]
       (db/db)))

(defn all-articles []
  (let [result-ids (find-all-article-ids)]
    (map (->> % first (d/entity (db/db)))
         result-ids)))
