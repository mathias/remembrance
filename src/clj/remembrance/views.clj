(ns remembrance.views
  (:use [hiccup core page]))

(defn index-page []
  (html5
        [:head
         [:title "Hello world"]
         (include-css "css/style.css")]
        [:body
         [:h1 "Hello world"]]))
