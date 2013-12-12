(ns remembrance.workers
  (:require [remembrance.config :as config]
            [remembrance.models.article :refer [article-ingest article-get-original-html]]
            [taoensso.carmine :as car :refer (wcar)]
            [taoensso.carmine.message-queue :as car-mq]
            [taoensso.timbre :refer [info]]))

(def env (config/load!))

(def redis-uri (env :redis-uri))

(def server-conn {:pool {} :spec {:uri redis-uri}})

(defmacro wcar* [& body] `(car/wcar server-conn ~@body))

(defn ping-redis []
  (wcar* (car/ping)))

(def article-ingest-worker
  (car-mq/worker {:spec {:uri redis-uri}} "article-ingest-queue"
                 {:handler (fn [{:keys [message]}]
                             (info "Article Ingest Worker got work:" message)
                             {:status (article-ingest message)})
                  :throttle-ms 500
                  :eoq-backoff-ms 100}))

(defn enqueue-article-ingest [article-guid]
  (wcar* (car-mq/enqueue "article-ingest-queue" article-guid)))

(def article-original-html-worker
  (car-mq/worker {:spec {:uri redis-uri}} "article-original-html-queue"
                 {:handler (fn [{:keys [message]}]
                             (info "Article Original HTML Worker got work:" message)
                             {:status (article-get-original-html message)})
                  :throttle-ms 500
                  :eoq-backoff-ms 100}))

(defn enqueue-article-original-html [article-guid]
  (wcar* (car-mq/enqueue "article-original-html-queue" article-guid)))
