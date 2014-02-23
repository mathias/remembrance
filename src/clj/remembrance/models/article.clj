(ns remembrance.models.article
  (:require ;;[clojure.data.json :as json]
            ;;[cemerick.url :refer [url url-encode]]
            [datomic.api :as d]
            ;;[org.httpkit.client :as http]
            [remembrance.config :as config]
            [remembrance.database :refer [db]]
            [remembrance.models.core :refer [first-entity]]
            ;;[taoensso.timbre :refer [info]]
            ;;[hearst.url-cleanup :refer [normalize-url]]
            ))

(defn create-article [params])

(defn update-article [params])

(defn mark-article-as-read [guid])

(defn find-all-ingested-articles-q [db]
  (d/q '[:find ?eid
         :where [?eid :article/ingest_state :article.ingest_state/ingested]]
       db))

(defn find-all-ingested-articles
  ([] (find-all-ingested-articles (db)))
  ([db]
     (map (partial first-entity db) (find-all-ingested-articles-q db))))

(defn find-article-by-guid-q [db guid]
  (d/q '[:find ?eid
         :in $ ?guid
         :where [?eid :article/guid ?guid]]
       db
       guid))

(defn find-article-by-guid
  ([guid] (find-article-by-guid (db) guid))
  ([db guid]
     (first-entity db (first (find-article-by-guid-q db guid)))))

(defn search-articles-q [db query]
  (d/q '[:find ?e
         :in $ % ?query
         :where (search-rules ?query ?e)]
       db
       '[[(search-rules ?query ?e)
          [(fulltext $ :article/readable_body ?query) [[?e]]]]
         [(search-rules ?query ?e)
          [(fulltext $ :article/title ?query) [[?e]]]]]
       query))

(defn search-articles
  ([query]
     (search-articles (db) query))
  ([db query]
     (let [results (search-articles-q db query)]
       (map (partial first-entity db) results))))

(defn count-read-articles-q [db]
  (ffirst (d/q '[:find (count ?e)
                 :where [?e :article/read true]]
               db)))

(defn count-read-articles
  ([] (count-read-articles (db)))
  ([db] (or
         (count-read-articles-q db)
         0)))

(defn count-articles-of-ingest-state [ingest-state])


(defn articles-stats [])
