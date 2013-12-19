(ns remembrance.models.article
  (:require [clojure.data.json :as json]
            [cemerick.url :refer [url url-encode]]
            [datomic.api :as d]
            [org.httpkit.client :as http]
            [remembrance.config :as config]
            [remembrance.database :as database]))

(def env (config/load!))
(def wolfcastle-uri (env :wolfcastle-uri))

(defn db []
  (database/db))

(defn entity [eid]
  (d/entity (db) eid))

(defn article-guid [article]
  (:article/guid article))

(defn article-entity [entity-vec]
  (entity (first entity-vec)))

(defn find-article-by-guid [guid]
  (d/q '[:find ?eid
         :in $ ?guid
         :where [?eid :article/guid ?guid]]
       (db)
       guid))

(defn find-one-article-by-guid [guid]
  (->> guid
       (find-article-by-guid)
       (ffirst)
       (entity)))

(defn show-article [guid]
  (let [results (find-article-by-guid guid)]
    (d/touch (entity (ffirst results)))))

(defn find-article-by-original-url [original-url]
  (d/q '[:find ?e
         :in $ ?original_url
         :where [?e :article/original_url ?original_url]]
       (db)
       original-url))

(defn create-article-tx [guid original-url]
  (database/t [{:db/id (d/tempid "db.part/user")
                :article/guid guid
                :article/original_url original-url
                :article/ingest_state "new"
                :article/read false}]))

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

(defn set-article-read-status [article read?]
  (database/t [{:db/id (:db/id article)
                :article/read read?}]))

(defn find-all-article-ids []
  (d/q '[:find ?a
         :where [?a :article/guid]]
       (db)))

(defn all-articles []
  (let [result-ids (find-all-article-ids)]
    (map article-entity result-ids)))

(defn zip-article-list-item-keys [article-row]
  (zipmap [:guid :title :original_url] article-row))

(defn find-all-ingested-articles []
  (map article-entity (d/q '[:find ?a
                             :where [?a :article/ingest_state ?ingest_state]
                                    [(not= "errored" ?ingest_state)]]
                           (db))))

(defn fetch-original-html [article]
  (let [original-url (:article/original_url article)
        original-html (get @(http/get original-url) :body)]
    (str original-html)))

(defn wolfcastle-url [url-to-ingest]
  (str (assoc (url wolfcastle-uri) :query {:url (url-encode url-to-ingest)})))

(defn get-readable-article [article]
  (json/read-str
   (or
    (get @(http/get (wolfcastle-url (:article/original_url article))) :body)
    "{}")
   :key-fn keyword))

(defn set-article-as-errored [article]
  @(database/t [{:db/id (:db/id article)
                 :article/ingest_state "errored"}])
  :error)

(defn update-readable-html-txn [article readable-article]
  (let [title (or (:title readable-article) "")
        body (or (:html readable-article) "")]
    @(database/t [{:db/id (:db/id article)
                   :article/title title
                   :article/readable_body body
                   :article/ingest_state "ingested"}]))
  :success)

(defn update-readable-html [article]
  (if-let [readable-article (get-readable-article article)]
    (update-readable-html-txn article readable-article)
    (set-article-as-errored article)))

(defn article-extract-text [guid]
  (if-let [article (find-one-article-by-guid guid)]
    (update-readable-html article)
    :error))

(defn update-original-html-txn [article article-html]
  @(database/t [{:db/id (:db/id article)
                 :article/original_html article-html
                 :article/ingest_state "fetched"}])
  :success)

(defn update-original-html [article]
  (if-let [article-html (fetch-original-html article)]
    (update-original-html-txn article article-html)
    (set-article-as-errored article)))

(defn article-get-original-html [guid]
  (if-let [article (find-one-article-by-guid guid)]
    (update-original-html article)
    :error))

(defn search-article-attributes [query-string]
  (d/q '[:find ?e
         :in $ % ?query
         :where (article-search-rules ?query ?e)]
       (db)
       '[[(article-search-rules ?query ?e)
         [(fulltext $ :article/readable_body ?query) [[?e]]]]
        [(article-search-rules ?query ?e)
         [(fulltext $ :article/title ?query) [[?e]]]]]
       query-string))

(defn search-articles [query-string]
  (let [results (search-article-attributes query-string)]
    (map article-entity results)))

(defn count-articles
  ([]
     (ffirst (d/q '[:find (count ?e)
                    :where [?e :article/guid _]]
                  (db))))
  ([state]
     (or (ffirst (d/q '[:find (count ?e)
                         :in $ ?state
                         :where [?e :article/guid _]
                         [?e :article/ingest_state ?state]]
                       (db)
                       state))
         0)))
