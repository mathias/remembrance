(ns remembrance.scripts.normalize-urls
  (:require [remembrance.models.article :refer :all]
            [remembrance.database :as database]
            [hearst.url-cleanup :refer [normalize-url]]))

(def ^:dynamic *updated-count* (atom 0))

(defn update-article-with-normalized-url [article]
  (let [url (:article/original_url article)
        normalized-url (str (normalize-url url))]
    (if-not (= url normalized-url)
      (do
        @(database/t [{:db/id (:db/id article)
                       :article/original_url normalized-url}])
        (swap! *updated-count* inc)))))

(defn mark-article-as-errored [article]
  (println "Could not parse: " (:article/original_url article))
  (set-article-as-errored article))

(defn -main []
  (time
   (doseq [article (all-articles)]
     (try (update-article-with-normalized-url article)
          (catch java.net.MalformedURLException e
            (mark-article-as-errored article)))))
  (println "Normalizing URLs complete!")
  (println @*updated-count* "articles updated.")
  (System/exit 0))
