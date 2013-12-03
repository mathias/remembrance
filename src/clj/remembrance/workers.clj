(ns remembrance.workers
  (:require [remembrance.config :as config]
            [remembrance.models.article :refer [article-ingest]]
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
  (car-mq/worker {:pool {} :spec {:uri redis-uri}}
                 "article-ingest-queue"
                 {:handler (fn [{:keys [message]}]
                             (info "Article Ingest Worker got work:" message)
                             (article-ingest message)
                             {:status :success})
                  ;; enable to log dry runs on each tick:
                  ;; :monitor (fn [{:keys [mid-circle-size ndry-runs poll-reply]}] (info "dry runs:" ndry-runs))
                  :throttle-ms 500
                  :eoq-backoff-ms 100}))

(defn enqueue-article-ingest [article-guid]
  (wcar* (car-mq/enqueue "article-ingest-queue" article-guid)))
