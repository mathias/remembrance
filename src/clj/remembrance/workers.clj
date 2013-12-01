(ns remembrance.workers
  (:require [remembrance.config :as config]
            [remembrance.db :as db]
            [taoensso.carmine :as car :refer (wcar)]
            [taoensso.carmine.message-queue :as car-mq]
            [datomic.api :as d]
            [taoensso.timbre :refer [info]]))

(def env (config/load!))

(def server-conn {:pool {} :spec {:uri (env :redis-uri)}})

(defmacro wcar* [& body] `(car/wcar server-conn ~@body))

(defn ping-redis []
  (wcar* (car/ping)))

(defn article-ingest [guid]
  (let [results (d/q '[:find ?eid :in $ ?guid :where [?eid :article/guid ?guid]] (db/db) guid)
        eid (ffirst results)
        article (d/entity (db/db) eid)
        original-url (:article/original_url article)
        original-html (slurp original-url)]
    @(db/t [{:db/id (:db/id article)
             :article/original_html original-html
             :article/ingest_state "fetched"}])

    ;; (info (slurp (article :original_url)))
    ;; slurp original HTML and save it
    ;; request sanitized body from wolfcastle and save it
    {:status :success}))


(def article-ingest-worker
  (car-mq/worker {:pool {} :spec {:uri (env :redis-uri)}}
                 "article-ingest-queue"
                 {:handler (fn [{:keys [message attempt lock-ms eoq-backoff-ms nthreads throttle-ms]}]
                             (info "Article Ingest Worker got work:" message)
                             ;; get the article entity
                             (article-ingest message))}))

(defn enqueue-article-ingest [article-guid]
  (wcar* (car-mq/enqueue "article-ingest-queue" article-guid)))
