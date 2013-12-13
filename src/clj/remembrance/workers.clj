(ns remembrance.workers
  (:require [remembrance.config :as config]
            [remembrance.models.article :refer [article-get-original-html article-extract-text]]
            [taoensso.carmine :as car :refer (wcar)]
            [taoensso.carmine.message-queue :as car-mq]
            [taoensso.timbre :refer [info]]))

(def env (config/load!))

(def redis-uri (env :redis-uri))

(def server-conn {:pool {} :spec {:uri redis-uri}})

(defmacro wcar* [& body] `(car/wcar server-conn ~@body))

(defn ping-redis []
  (wcar* (car/ping)))

(def article-text-extraction
  (car-mq/worker {:spec {:uri redis-uri}} "article-text-extraction-queue"
                 {:handler (fn [{:keys [message]}]
                             (info "Article Text Extraction Worker got work:" message)
                             {:status (article-extract-text message)})
                  :throttle-ms 500
                  :eoq-backoff-ms 100}))

(defn enqueue-text-extraction [article-guid]
  (wcar* (car-mq/enqueue "article-text-extraction-queue" article-guid)))

(defn enqueue-next-step-and-return-success [guid]
  (enqueue-text-extraction guid)
  {:status :success})

(def article-original-html-worker
  (car-mq/worker {:spec {:uri redis-uri}} "article-original-html-queue"
                 {:handler (fn [{:keys [message]}]
                             (info "Article Original HTML Worker got work:" message)
                             (if (= :success (article-get-original-html message))
                               (enqueue-next-step-and-return-success message)
                               {:status :error}))
                  :throttle-ms 500
                  :eoq-backoff-ms 100}))

(defn enqueue-article-original-html [article-guid]
  (wcar* (car-mq/enqueue "article-original-html-queue" article-guid)))
