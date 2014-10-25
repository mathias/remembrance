(ns remembrance.fetchers.newspaper
  (:require [remembrance.models.article :refer [data-import-article]]
            [org.httpkit.client :as http]
            [cemerick.url :refer [url url-encode]]
            [environ.core :refer [env]]
            [taoensso.timbre :refer [info error]]
            [cheshire.core :refer [parse-string]]))

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
