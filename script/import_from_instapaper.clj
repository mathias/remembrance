(require '[remembrance.db :as db]
         '[remembrance.models.article :refer [create-article article-ingest find-one-article-by-guid]]
         '[clojure.java.io :as io]
         '[clojure-csv.core :as csv])

(defn parse-row [row]
  (let [v (first (csv/parse-csv row))]
    (zipmap [:original_url :title :selection :folder] v)))

(defn ingest-instapaper-csv [filename]
   (with-open [rdr (io/reader filename)]
     (doseq [line (line-seq rdr)]
       (let [article (create-article (parse-row line))
             guid (:article/guid article)]
         ;; run synchronously rather than in queue
         (article-ingest guid)
         (println "Ingested: " (:article/title (find-one-article-by-guid guid)))))))

(do
  (def csv-file (second *command-line-args*))
  (ingest-instapaper-csv csv-file))
