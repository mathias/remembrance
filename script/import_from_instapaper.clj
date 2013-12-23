(require '[remembrance.models.article :refer :all]
         '[clojure.java.io :as io]
         '[clojure-csv.core :as csv])

(defn parse-row [row]
  (let [v (first (csv/parse-csv row))]
    (zipmap [:original_url :title :selection :folder] v)))

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
    (println "Error creating article:"(:original_url attrs))))

(defn valid-row? [attrs]
  (not (nil? (:original_url attrs))))

(defn ingest-instapaper-csv [filename]
   (with-open [rdr (io/reader filename)]
     (doseq [line (rest (line-seq rdr))] ;; we get "rest" to ignore the column names on the first
       (let [attrs (parse-row line)]
         (if (valid-row? attrs)
           (create-and-ingest-article attrs)
           (println "Error. Not a valid record:" (or (:title attrs)
                                                     (:original_url attrs))))))))

(do
  (def csv-file (second *command-line-args*))
  (ingest-instapaper-csv csv-file))

(println "Import complete!")
(System/exit 0)
