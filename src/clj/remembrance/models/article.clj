(ns remembrance.models.article
  (require [remembrance.db :as db]
           [remembrance.workers :refer [enqueue-article-ingest]]
           [datomic.api :as d]
           [taoensso.timbre :refer [info]]))

(defn entity [eid]
  (d/entity (db/db) eid))

(defn article-guid [article]
  (info article)
  (info (get article :article/guid))
  (get article :article/guid))

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
  (d/q '[:find ?e
         :in $ ?original_url
         :where [?e :article/original_url ?original_url]]
       (db/db)
       original-url))

(defn create-article-tx [guid original-url]
  (db/t [{:db/id (d/tempid "db.part/user")
          :article/guid guid
          :article/original_url original-url
          :article/ingest_state "new"}]))

(defn create-article [attrs]
  ;; try to find an existing article first
  (let [original-url (attrs :original_url)
        existing (find-article-by-original-url original-url)]
    (if (empty? existing)
      (let [guid (str (d/squuid))]
        ;; create the new article:
        (create-article-tx guid original-url)
        (enqueue-article-ingest guid)
        ;; return guid so that it can redirect to article
        guid)
      (article-guid (first existing)))))

(defn find-all-article-ids []
  (d/q '[:find ?a
         :where [?a :article/guid]]
       (db/db)))

(defn all-articles []
  (let [result-ids (find-all-article-ids)]
    (map (fn [result-id] (entity (first result-id))) result-ids)))
