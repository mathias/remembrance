(ns remembrance.views
  (:require [hiccup.page :refer :all]
            [remembrance.models.article :refer [count-articles count-read-articles]]))

(defn index-page []
  (let [total-articles-count (count-articles)]
    (html5
     [:head
      [:title "Remembrance"]
      (include-css "css/style.css")]
     [:body
      [:h1 "Hello world"]
      [:p "What am I?"]
      [:h2 "Stats"]
      [:p (str "There are " total-articles-count " articles in the system.")]
      [:p (str (count-articles :article.ingest_state/ingested)
               " ingested, "
               (count-articles :article.ingest_state/fetched)
               " fetched, and "
               (count-articles :article.ingest_state/errored)
               " errored.")]
      [:p (str "You have read " (count-read-articles) " out of " total-articles-count ".")]])))
