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

(defn ingest-instapaper-csv [filename]
   (with-open [rdr (io/reader filename)]
     (doseq [line (line-seq rdr)]
       (let [attrs (parse-row line)
             article (create-article attrs)]
         ;; Always try to update the read status
         (if (= "Archive" (:folder attrs))
           (set-article-read-status article true))
         (if-not (= "ingested" (:article/ingest_state article))
           (ingest-article (:article/guid article))
           (println "Already have" (:original_url attrs)))))))

(do
  (def csv-file (second *command-line-args*))
  (ingest-instapaper-csv csv-file))

(println "Import complete!")
(System/exit 0)
