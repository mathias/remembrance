(ns remembrance.models.article
  (:require [datomic.api :as d]
            [hearst.url-cleanup :refer [normalize-url]]
            [remembrance.database :refer [db new-guid]]
            [remembrance.models.core :refer [first-entity]]))

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
     (->> guid
          (find-article-by-guid-q db)
          (first)
          (first-entity db))))

(defn find-article-by-original-url-q [db original-url]
  (d/q '[:find ?eid
         :in $ ?original_url
         :where [?eid :article/original_url ?original_url]]
       db
       original-url))

(defn find-article-by-original-url
  ([original-url] (find-article-by-original-url (db) original-url))
  ([db original-url]
     (->> original-url
          (find-article-by-original-url-q db)
          first
          (first-entity db))))

(defn find-articles-by-tag-q [db tag]
  (d/q '[:find ?eid
         :in $ ?tag
         :where [?eid :article/tags ?tag]]
       db
       tag))

(defn find-articles-by-tag
  ([tag] (find-articles-by-tag (db) tag))
  ([db tag]
     (->> tag
          (find-articles-by-tag-q db)
          (map #(first-entity db %)))))

(defn find-articles-by-keyword-q [db keyword]
  (d/q '[:find ?eid
         :in $ ?keyword
         :where [?eid :article/keywords ?keyword]]
       db
       (clojure.string/lower-case keyword)))

(defn find-articles-by-keyword
  ([keyword] (find-articles-by-keyword (db) keyword))
  ([db keyword]
     (->> keyword
          (find-articles-by-keyword-q db)
          (map #(first-entity db %)))))


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

(def allowed-articles-keys-for-creation
  [:article/original_url
   :article/guid])

(def allowed-articles-keys-for-user-update
  [:article/read])

(def allowed-articles-keys-for-data-import
  [:article/read
   :article/title
   :article/readable_body
   :article/plain_text_body
   :article/original_html
   :article/ingest_state
   :article/date_published
   :article/date_fetched
   :article/date_ingested
   :article/authors
   :article/keywords
   :article/meta_language
   :article/summary
   :article/tags])

(def article-keys-translations
  {:original_url    :article/original_url
   :url             :article/original_url
   :guid            :article/guid
   :read            :article/read
   :title           :article/title
   :readable_body   :article/readable_body
   :plain_text_body :article/plain_text_body
   :original_html   :article/original_html
   :ingest_state    :article/ingest_state
   :date_published  :article/date_published
   :date_fetched    :article/date_fetched
   :date_ingested   :article/date_ingested
   :authors         :article/authors
   :keywords        :article/keywords
   :meta_language   :article/meta_language
   :summary         :article/summary
   :tags            :article/tags})

(defn translate-create-key-names [params]
  (-> params
      (clojure.set/rename-keys article-keys-translations)
      (select-keys allowed-articles-keys-for-creation)))

(defn translate-data-import-key-names [params]
  (-> params
      (clojure.set/rename-keys article-keys-translations)
      (select-keys allowed-articles-keys-for-data-import)))

(defn translate-update-key-names [params]
  (-> params
      (clojure.set/rename-keys article-keys-translations)
      (select-keys allowed-articles-keys-for-user-update)))

(defn translate-update-values [params]
  (if (= (type (:article/read params)) java.lang.String)
    (update-in params [:article/read] read-string)
    params))

(defn translate-data-import-keys-and-values [params]
  (-> params
      (translate-data-import-key-names)
      (translate-update-values)))

(defn translate-update-keys-and-values [params]
  (-> params
      (translate-update-key-names)
      (translate-update-values)))

(defn translate-create-values [params]
  (if (contains? params :article/original_url)
    (update-in params [:article/original_url] normalize-url)
    params))

(defn translate-create-keys-and-values [params]
  (-> params
      (translate-create-key-names)
      (translate-create-values)))

(defn create-article-txn [conn attributes]
  (d/transact conn
              [(merge {:db/id (d/tempid "db.part/user")
                       :article/ingest_state :article.ingest_state/new
                       :article/read false}
                      attributes)]))

(defn create-article
  ([params] (create-article remembrance.database/connection params))
  ([conn params]
     (let [translated-attrs (translate-create-keys-and-values params)
           original-url (:article/original_url translated-attrs)]
       (if-let [article (find-article-by-original-url (d/db conn) original-url)]
         article
         ;; create and return new article
         (let [guid (new-guid)
               article-attrs (assoc translated-attrs :article/guid guid)]
           (create-article-txn conn article-attrs)
           (find-article-by-original-url (d/db conn) original-url))))))

(defn update-article-txn [conn article attributes]
  (d/transact conn
              [(merge {:db/id (:db/id article)}
                      attributes)]))

(defn update-article [conn article params]
  (when-let [guid (:article/guid article)]
    (update-article-txn conn article (translate-update-keys-and-values params))
    (find-article-by-guid (d/db conn) guid)))

(defn data-import-article [conn article params]
  (when-let [guid (:article/guid article)]
    (update-article-txn conn article (translate-data-import-keys-and-values params))
    (find-article-by-guid (d/db conn) guid)))

(defn mark-article-as-read [conn guid]
  (when-let [article (find-article-by-guid (d/db conn) guid)]
    (update-article-txn conn article {:article/read true})
    (find-article-by-guid (d/db conn) guid)))

(defn mark-article-as-unread [conn guid]
  (when-let [article (find-article-by-guid (d/db conn) guid)]
    (update-article-txn conn article {:article/read false})
    (find-article-by-guid (d/db conn) guid)))

(defn count-all-articles-q [db]
  (d/q '[:find (count ?e)
         :where [?e :article/guid _]]
       db))

(defn count-all-articles
  ([] (count-all-articles (db)))
  ([db]
     (-> (count-all-articles-q db)
         (ffirst)
         (or 0))))

(defn count-read-articles-q [db]
  (d/q '[:find (count ?e)
         :where [?e :article/read true]]
       db))

(defn count-read-articles
  ([] (count-read-articles (db)))
  ([db]
     (-> (count-read-articles-q db)
         (ffirst)
         (or 0))))

(defn count-articles-with-ingest-state-q [db ingest-state]
  (d/q '[:find (count ?eid)
         :in $ ?state
         :where [?eid :article/guid _]
         [?eid :article/ingest_state ?state]]
       db
       ingest-state))

(defn count-articles-with-ingest-state
  ([ingest-state] count-articles-with-ingest-state (db) ingest-state)
  ([db ingest-state]
     (-> (count-articles-with-ingest-state-q db ingest-state)
         (ffirst)
         (or 0))))
