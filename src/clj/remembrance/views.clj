(ns remembrance.views
  (:require [hiccup.page :refer :all]))

(defn index-page []
  (html5
        [:head
         [:title "Hello world"]
         (include-css "css/style.css")]
        [:body
         [:h1 "Hello world"]
         [:p "What am I?"]]))
