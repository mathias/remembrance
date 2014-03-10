(ns remembrance.views
  (:require [hiccup.page :refer :all]
            [remembrance.models.article :refer [count-all-articles
                                                count-articles-with-ingest-state
                                                count-read-articles]]
            [remembrance.models.note :refer [count-notes]]))

(defn index-page []
  (let [total-articles-count (count-all-articles)
        total-notes-count (count-notes)]
    (html5
     [:head
      [:title "Remembrance"]
      (include-css "css/style.css")]
     [:body
      [:h1 "Hello world"]
      [:p "What am I?"]
      [:h2 "Stats"]
      [:h3 "Articles"]
      [:ul
       [:li (str "There are " total-articles-count " articles in the system.")]
       [:li (str (count-articles-with-ingest-state :article.ingest_state/ingested)
                " ingested, "
                (count-articles-with-ingest-state :article.ingest_state/fetched)
                " waiting to be ingested, and "
                (count-articles-with-ingest-state :article.ingest_state/errored)
                " errored.")]
       [:li (str "You have read " (count-read-articles) " out of " total-articles-count ".")]]
      [:h3 "Notes"]
      [:ul
       [:li (str "There are " total-notes-count " notes in the system.")]]])))
