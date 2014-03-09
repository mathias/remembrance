(ns remembrance.workers
  (:require [remembrance.config :refer [production-env?]]
            [taoensso.timbre :refer [info]]))

(defn enqueue-article-original-html [article-guid])
