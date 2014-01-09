(ns remembrance.views
  (:require [hiccup.page :refer :all]
            [remembrance.models.article :refer [count-articles count-read-articles]]
            [remembrance.models.note :refer [count-notes]]))

(defn index-page []
  (let [total-articles-count (count-articles)
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
       [:li (str (count-articles :article.ingest_state/ingested)
                " ingested, "
                (count-articles :article.ingest_state/fetched)
                " fetched, and "
                (count-articles :article.ingest_state/errored)
                " errored.")]
       [:li (str "You have read " (count-read-articles) " out of " total-articles-count ".")]]
      [:h3 "Notes"]
      [:ul
       [:li (str "There are " total-notes-count " notes in the system.")]]])))
