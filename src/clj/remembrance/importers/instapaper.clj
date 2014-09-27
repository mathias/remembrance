(ns remembrance.importers.instapaper
  (:require [remembrance.models.article :refer :all]
            [clojure-csv.core :as csv]
            [clojure.walk :refer [keywordize-keys]]
            [clojure.string :as string]
            [clojure.core.async :refer [chan go <! >!]]
            [taoensso.timbre :refer [info]]))

(def create-and-ingest-article-chan (chan))

(defn parse-csv [csv-data]
  (let [[headers & rows] (csv/parse-csv csv-data)
        lowercased-headers (map string/lower-case headers)]
    (map #(->> % (zipmap lowercased-headers) keywordize-keys) rows)))

(defn create-and-import-article [article-data]
  (let [new-article (create-article article-data)]
    ))

(defn import-from-post [csv-data]
  (let [articles-to-import (parse-csv csv-data)]
    (map create-and-import-article articles-to-import)))
