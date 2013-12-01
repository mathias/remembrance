(ns remembrance.workers
  (:require [remembrance.config :as config]
            [remembrance.db :as db]
            [taoensso.carmine :as car :refer (wcar)]
            [taoensso.carmine.message-queue :as car-mq]
            [cemerick.url :refer [url url-encode]]
            [datomic.api :as d]
            [clojure.data.json :as json]
            [taoensso.timbre :refer [info]]))

(def env (config/load!))

(def redis-uri (env :redis-uri))

(def wolfcastle-uri (env :wolfcastle-uri))

(def server-conn {:pool {} :spec {:uri redis-uri}})

(defmacro wcar* [& body] `(car/wcar server-conn ~@body))

(defn ping-redis []
  (wcar* (car/ping)))

(defn wolfcastle-url [url-to-ingest]
  (str (assoc (url wolfcastle-uri) :query {:url (url-encode url-to-ingest)})))


(defn article-ingest [guid]
  (let [results (d/q '[:find ?eid :in $ ?guid :where [?eid :article/guid ?guid]] (db/db) guid)
        eid (ffirst results)
        article (d/entity (db/db) eid)
        original-url (:article/original_url article)
        original-html (slurp original-url)
        original-html-tx @(db/t [{:db/id (:db/id article)
                                  :article/original_html original-html
                                  :article/ingest_state "fetched"}])
        wolfcastle-response (slurp (wolfcastle-url original-url))
        parsed-wolfcastle-response (json/read-str wolfcastle-response :key-fn keyword)
        readable-title (parsed-wolfcastle-response :title)
        readable-body (parsed-wolfcastle-response :html)
        readable-body-tx @(db/t [{:db/id (:db/id article)
                                  :article/readable_body readable-body
                                  :article/title readable-title
                                  :article/ingest_state "ingested"}])]
    (info "Fetched readable:" readable-title)
    (if (and original-html-tx
             readable-body-tx)
      {:status :success}
      {:status :error})))


(def article-ingest-worker
  (car-mq/worker {:pool {} :spec {:uri redis-uri}}
                 "article-ingest-queue"
                 {:handler (fn [{:keys [message attempt lock-ms eoq-backoff-ms nthreads throttle-ms]}]
                             (info "Article Ingest Worker got work:" message)
                             ;; get the article entity
                             (article-ingest message))
                  ;; enable to log dry runs on each tick:
                  ;; :monitor (fn [{:keys [mid-circle-size ndry-runs poll-reply]}] (info "dry runs:" ndry-runs))
                  :throttle-ms 500
                  :eoq-backoff-ms 100 ;;#(car-mq/exp-backoff (/ % 10))
                  }))

(defn enqueue-article-ingest [article-guid]
  (wcar* (car-mq/enqueue "article-ingest-queue" article-guid)))
