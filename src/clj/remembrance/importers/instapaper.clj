(ns remembrance.importers.instapaper
  (:require [remembrance.models.article :refer :all]
            [clojure.java.io :as io]
            [clojure-csv.core :as csv]
            [clojure.walk :as walk]
            [clojure.string :as string]
            [hearst.url-cleanup :refer [normalize-url]]
            [clojure.core.async :refer [chan go <! >!]]))

(def create-and-ingest-article-chan (chan))

(defn parse-file [csv-data]
  (let [[headers & rows] (csv/parse-csv csv-data)
        lowercased-headers (map string/lower-case headers)]
    (map #(->> % (zipmap lowercased-headers) walk/keywordize-keys) rows)))



