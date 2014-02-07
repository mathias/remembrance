(ns remembrance.scripts.import_from_instapaper
  (:require [remembrance.models.article :refer :all]
            [clojure.java.io :as io]
            [clojure-csv.core :as csv]
            [clojure.walk :as walk]
            [clojure.string :as str]))

(defn parse-file [csv-data]
  (let [[header & rows] (csv/parse-csv csv-data)
        lowercased-headers (map str/lower-case header)]
    (map #(->> % (zipmap lowercased-headers) walk/keywordize-keys) rows)))

(defn ingest-article [guid]
  (article-get-original-html guid)
  (article-extract-text guid)
  (println "Ingested: " (:article/title (find-one-article-by-guid guid))))

(defn ingest-and-set-state [article attrs]
  ;; Always try to update the read status
  (if (= "Archive" (:folder attrs))
    (set-article-read-status article true))
  (if-not (= :article.ingest_state/ingested (:article/ingest_state article))
    (ingest-article (:article/guid article))
    (println "Already have:" (:article/original_url article))))

(defn create-and-ingest-article [attrs]
  (if-let [article (create-article attrs)]
    (ingest-and-set-state article attrs)
    (println "Error creating article:"(:url attrs))))

(defn valid-row? [attrs]
  (not (nil? (:url attrs))))

(defn ingest-instapaper-csv [filename]
  (doseq [row (parse-file (slurp filename))]
    (when (valid-row? row)
      (create-and-ingest-article row))))

(defn -main [csv-file]
  (time (ingest-instapaper-csv csv-file))
  (println "Import complete!")
  (System/exit 0))
