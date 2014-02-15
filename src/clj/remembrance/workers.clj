(ns remembrance.workers
  (:require [remembrance.models.article :refer [article-get-original-html article-extract-text]]
            [clojure.core.async :refer [chan go <! >!]]
            [taoensso.timbre :refer [info]]))

(defonce article-original-html-channel (chan))
(defonce article-extract-text-channel (chan))

(defn process-text-extraction [article-guid]
  (do
    (info "Article text extraction worker got:" article-guid)
    (article-extract-text article-guid)))

(defn enqueue-text-extraction [article-guid]
  (go (>! article-extract-text-channel article-guid)))

(defn process-original-html [article-guid]
  (do
    (info "Original HTML worker got:" article-guid)
    (if (= :success (article-get-original-html article-guid))
      (enqueue-text-extraction article-guid))))
(go
 (while true
   (process-text-extraction (<! article-extract-text-channel))))

(go
 (while true
   (process-original-html (<! article-original-html-channel))))


(defn enqueue-article-original-html [article-guid]
  (go (>! article-original-html-channel article-guid)))
