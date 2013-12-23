(ns remembrance.views
  (:require [hiccup.page :refer :all]
            [remembrance.models.article :refer [count-articles]]))

(defn index-page []
  (html5
        [:head
         [:title "Hello world"]
         (include-css "css/style.css")]
        [:body
         [:h1 "Hello world"]
         [:p "What am I?"]
         [:h2 "Stats"]
         [:p (str "There are " (count-articles) " articles in the system.")]
         [:p (str (count-articles :article.ingest_state/ingested)
                  " ingested, "
                  (count-articles :article.ingest_state/fetched)
                  " fetched, and "
                  (count-articles :article.ingest_state/errored)
                  " errored.")]]))
