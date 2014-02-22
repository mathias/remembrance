(ns remembrance.scripts.import_from_instapaper
  (:require [remembrance.models.article :refer :all]
            [clojure.java.io :as io]
            [clojure-csv.core :as csv]
            [clojure.walk :as walk]
            [clojure.string :as str]
            [hearst.url-cleanup :refer [normalize-url]]
            [clojure.core.async :refer [chan go <! >!]]))

(def create-article-chan (chan))
(def ingest-chan (chan))

(defn parse-file [csv-data]
  (let [[headers & rows] (csv/parse-csv csv-data)
        lowercased-headers (map str/lower-case headers)]
    (map #(->> % (zipmap lowercased-headers) walk/keywordize-keys) rows)))

(defn ingest-article [guid]
  (article-get-original-html guid)
  (article-extract-text guid)
  (println "Ingested:" (:article/title (first (find-article-by-guid guid)))))

(defn ingest-and-set-state [article attrs]
  ;; Always try to update the read status
  (if (= "Archive" (:folder attrs))
    (set-article-read-status article true))
  (if-not (= :article.ingest_state/ingested (:article/ingest_state article))
    (ingest-article (:article/guid article))
    (println "Already have:" (:article/original_url article))))

(defn create-and-ingest-article [attrs]
  (if-let [article (create-article attrs)]
    (go (>! ingest-chan [article attrs]))
    (println "Error creating article:"(:url attrs))))

(defn valid-row? [attrs]
  (not (nil? (:url attrs))))

(go (while true
      (let [row (<! create-article-chan)]
        (create-and-ingest-article row))))

(go (while true
      (let [[article attrs] (<! ingest-chan)]
        (ingest-and-set-state article attrs))))

(defn ingest-instapaper-csv [filename]
  (go
   (doseq [row (parse-file (slurp filename))]
     (when (valid-row? row)
       (>! create-article-chan row)))))

(defn -main [csv-file]
  (ingest-instapaper-csv csv-file))
