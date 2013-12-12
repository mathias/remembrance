(require '[remembrance.models.article :refer :all]
         '[clojure.java.io :as io]
         '[clojure-csv.core :as csv])

(defn parse-row [row]
  (let [v (first (csv/parse-csv row))]
    (zipmap [:original_url :title :selection :folder] v)))

(defn ingest-article [guid]
  (article-get-original-html guid)
  (article-ingest guid)
  (println "Ingested: " (:article/title (find-one-article-by-guid guid))))

(defn ingest-instapaper-csv [filename]
   (with-open [rdr (io/reader filename)]
     (doseq [line (line-seq rdr)]
       (let [article (create-article (parse-row line))
             guid (:article/guid article)]
         (if-not (= (:article/ingest_state article) "ingested")
           (ingest-article guid)
           (println "Already have" (:original_url (parse-row line))))))))

(do
  (def csv-file (second *command-line-args*))
  (ingest-instapaper-csv csv-file))

(println "Import complete!")
(System/exit 0)
