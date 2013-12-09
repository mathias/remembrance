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
         [:p (str "There are " (count-articles) " articles in the system.")]
         [:p (str (count-articles "ingested") " ingested and " (count-articles "error") " errored.")]]))
