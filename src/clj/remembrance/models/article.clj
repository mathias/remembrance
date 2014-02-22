(ns remembrance.models.article
  (:require ;;[clojure.data.json :as json]
            ;;[cemerick.url :refer [url url-encode]]
            [datomic.api :as d]
            ;;[org.httpkit.client :as http]
            [remembrance.config :as config]
            [remembrance.database :as database]
            ;;[taoensso.timbre :refer [info]]
            ;;[hearst.url-cleanup :refer [normalize-url]]
            ))

(defn create-article [params])

(defn update-article [params])

(defn find-all-ingested-articles [])

(defn find-article-by-guid-q [db guid])

(defn find-article-by-guid [guid])

(defn search-articles [query])

(defn articles-stats [])

(defn mark-article-as-read [guid])

(defn count-articles [])

(defn count-read-articles [])
