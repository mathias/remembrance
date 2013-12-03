(ns remembrance.models.article
  (require [remembrance.config :as config]
           [remembrance.db :as db]
           [datomic.api :as d]
           [cemerick.url :refer [url url-encode]]
           [clojure.data.json :as json]
           [org.httpkit.client :as http]
           [taoensso.timbre :refer [info]]))

(def env (config/load!))
(def wolfcastle-uri (env :wolfcastle-uri))

(defn entity [eid]
  (d/entity (db/db) eid))

(defn article-guid [article]
  (get article :article/guid))

(defn find-article-by-guid [guid]
  (d/q '[:find ?eid
         :in $ ?guid
         :where [?eid :article/guid ?guid]]
       (db/db)
       guid))

(defn find-one-article-by-guid [guid]
  (->> guid
       (find-article-by-guid)
       (ffirst)
       (entity)))

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
        ;; return guid so that it can redirect to article
        (find-one-article-by-guid guid))
      (entity (ffirst existing)))))

(defn find-all-article-ids []
  (d/q '[:find ?a
         :where [?a :article/guid]]
       (db/db)))

(defn all-articles []
  (let [result-ids (find-all-article-ids)]
    (map (fn [result-id] (entity (first result-id))) result-ids)))

(defn fetch-original-html [article]
  (let [original-url (:article/original_url article)]
    (@(http/get original-url) :body)))

(defn wolfcastle-url [url-to-ingest]
  (str (assoc (url wolfcastle-uri) :query {:url (url-encode url-to-ingest)})))

(defn get-readable-article [article]
  (json/read-str
   (@(http/get (wolfcastle-url (:article/original_url article))) :body)
   :key-fn keyword))

(defn update-original-html [article]
  (db/t [{:db/id (:db/id article)
          :article/original_html (fetch-original-html article)
          :article/ingest_state "fetched"}]))

(defn update-readable-html [article]
  (let [readable-article (get-readable-article article)]
    (db/t [{:db/id (:db/id article)
            :article/title (readable-article :title)
            :article/readable_body (readable-article :body)
            :article/ingest_state "ingested"}])))

(defn article-ingest [guid]
  (let [article (find-one-article-by-guid guid)]
    (update-original-html article)
    (update-readable-html article)
    article))
