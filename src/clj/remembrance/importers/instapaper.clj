(ns remembrance.importers.instapaper
  (:require [remembrance.models.article :refer :all]
            [remembrance.fetchers.newspaper :refer [fetch-from-newspaper-delivery]]
            [clojure.core.async :refer [chan go <! >! close!]]
            [clojure-csv.core :as csv]
            [clojure.walk :refer [keywordize-keys]]
            [clojure.string :refer [lower-case]]
            [taoensso.timbre :refer [info error]]))

(defn parse-csv [csv-data]
  (let [[headers & rows] (csv/parse-csv csv-data)
        lowercased-headers (map lower-case headers)]
    (map #(->> % (zipmap lowercased-headers) keywordize-keys) rows)))

(defn valid-row? [article-data]
  (not (nil? (get article-data :url))))

(defn set-read-status [db-conn article row]
  (when (= "Archive" (:folder row))
    (info "Setting article as read:" (get article :article/original_url))
    (mark-article-as-read db-conn (:article/guid article))))

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
