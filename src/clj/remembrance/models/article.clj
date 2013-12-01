(ns remembrance.models.article
  (require [remembrance.db :as db]
           [datomic.api :as d]
           [taoensso.timbre :refer [info]]))


(defn show-article [guid]
  (let [results (d/q '[:find ?eid :in $ ?guid :where [?eid :article/guid ?guid]] (db/db) guid)
        eid (ffirst results)]
    (d/entity (db/db) eid)))

(defn create-article [attrs]
  ;; try to find an existing one first
  (let [original-url {attrs :original_url}
        existing (d/q '[:find ?e :in $ ?original_url :where [?e :article/original_url ?original_url]] (db/db) original-url)]
    (if (empty? existing)
      (let [guid (str (d/squuid))]

        ;; create the new article:
        @(db/t [{:db/id (d/tempid "db.part/user")
                 :article/guid guid
                 :article/original_url original-url}])
        ;; redirect to article
        guid)
      ((first existing) :guid))))

(defn all-articles []
  (let [result-ids (d/q '[:find ?a :where [?a :article/guid]] (db/db))]
    (map (fn [result-id] (d/entity (db/db) (first result-id))) result-ids)))
