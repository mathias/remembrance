(ns remembrance.importers.instapaper
  (:require [remembrance.models.article :refer :all]
            [cemerick.url :refer [url url-encode]]
            [clojure-csv.core :as csv]
            [clojure.walk :refer [keywordize-keys]]
            [clojure.string :as string]
            [clojure.core.async :refer [chan go <! >!]]
            [environ.core :refer [env]]
            [org.httpkit.client :as http]
            [taoensso.timbre :refer [info]]))

(def create-and-ingest-article-chan (chan))

(defn parse-csv [csv-data]
  (let [[headers & rows] (csv/parse-csv csv-data)
        lowercased-headers (map string/lower-case headers)]
    (map #(->> % (zipmap lowercased-headers) keywordize-keys) rows)))

(defn newspaper-url [article-original-url]
  (-> (url (env :newspaper-delivery-uri))
      (assoc :path "/article")
      (assoc :query {:url (url-encode article-original-url)})
      str))

(defn fetch-from-newspaper-delivery [article]
  (:body @(http/get (newspaper-url (:original-url article)))))

(defn import-article [article]
  (let [response (fetch-from-newspaper-delivery article)]
    response))

(defn create-and-import-article [article-data]
  (let [new-article (create-article article-data)]
    (import-article new-article)))

(defn import-articles [csv-data]
  (let [articles-to-import (parse-csv csv-data)]
    (map create-and-import-article articles-to-import)))
