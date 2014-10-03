(ns remembrance.importers.instapaper
  (:require [remembrance.models.article :refer :all]
            [cemerick.url :refer [url url-encode]]
            [cheshire.core :refer [parse-string]]
            [clojure.core.async :refer [chan go <! >! close!]]
            [clojure-csv.core :as csv]
            [clojure.walk :refer [keywordize-keys]]
            [clojure.string :refer [lower-case]]
            [environ.core :refer [env]]
            [org.httpkit.client :as http]
            [taoensso.timbre :refer [info error]]))

(defn parse-csv [csv-data]
  (let [[headers & rows] (csv/parse-csv csv-data)
        lowercased-headers (map lower-case headers)]
    (map #(->> % (zipmap lowercased-headers) keywordize-keys) rows)))

(defn newspaper-url [article-original-url]
  (-> (env :newspaper-delivery-uri)
      url
      (assoc :path "/article")
      (assoc :query {:url article-original-url})
      str))

(defn update-article-with [response-body opts]
  (let [db-conn (:db-conn opts)
        article (:article opts)
        ingest-time (:ingest-time opts)
        response-data (parse-string response-body)]
    ;; TODO: create authors, if any

    (info "Fetched article:" (:article/original_url article))

    (data-import-article db-conn
                         article
                         {:title (get response-data "title" "")
                          :readable_body (get response-data "html" "")
                          :plain_text_body (get response-data "text" "")
                          :original_html (get response-data "original_html" "")
                          :ingest_state :article.ingest_state/ingested
                          :date_fetched ingest-time
                          :date_ingested ingest-time
                          :authors []
                          :keywords (get response-data "keywords" [])
                          :meta_language (get response-data "meta_lang" "")
                          :summary (get response-data "summary" "")
                          :tags (get response-data "tags" [])})))

(defn handle-newspaper-response
  [{:keys [status headers body error opts]}]
  (if (and (= status 200) (not error))
    (update-article-with body opts)
    (info "Article request failed:" (get-in opts [:article :article/original_url]))))

(defn fetch-from-newspaper-delivery [db-conn article]
  (let [opts {:article article
              :db-conn db-conn
              :ingest-time (java.util.Date.)}
        original-url (:article/original_url article)]
    (handle-newspaper-response @(http/get (newspaper-url original-url) opts))))

(defn valid-row? [article-data]
  (not (nil? (get article-data :url))))

(defn set-read-status [db-conn article row]
  (when (= "Archive" (:folder row))
     (mark-article-as-read db-conn article)))

(defn create-and-setup-article
  ([article-data] (create-and-setup-article remembrance.database/connection article-data))
  ([db-conn article-data]
     (let [new-article (create-article db-conn article-data)]
       (set-read-status db-conn new-article article-data)
       new-article)))

(defn import-articles
  ([csv-data] (import-articles remembrance.database/connection csv-data))
  ([db-conn csv-data]
     (let [import-chan (chan 4)]
       (go
         (doseq [row (parse-csv csv-data)]
           (when (valid-row? row)
             (let [article (create-and-setup-article db-conn row)]
               (when (not= (:article/ingest_state article) :article.ingest_state/ingested)
                 (>! import-chan [db-conn article]))))))

       (go
         (while true
           (let [[db-conn article] (<! import-chan)]
             (fetch-from-newspaper-delivery db-conn article)))))))

(comment
  (def article-csv (slurp "instapaper-export.csv"))
  (import-articles article-csv))
