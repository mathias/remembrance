(ns remembrance.workers
  (:require [remembrance.config :as config]
            [remembrance.db :as db]
            [remembrance.models.article :refer [find-one-article-by-guid]]
            [taoensso.carmine :as car :refer (wcar)]
            [taoensso.carmine.message-queue :as car-mq]
            [cemerick.url :refer [url url-encode]]
            [datomic.api :as d]
            [clojure.data.json :as json]
            [org.httpkit.client :as http]
            [taoensso.timbre :refer [info]]))

(def env (config/load!))

(def redis-uri (env :redis-uri))

(def wolfcastle-uri (env :wolfcastle-uri))

(def server-conn {:pool {} :spec {:uri redis-uri}})

(defmacro wcar* [& body] `(car/wcar server-conn ~@body))

(defn ping-redis []
  (wcar* (car/ping)))

(defn fetch-original-html [article]
  (let [original-url (:article/original_url article)]
    (@(http/get original-url) :body)))

(defn wolfcastle-url [url-to-ingest]
  (str (assoc (url wolfcastle-uri) :query {:url (url-encode url-to-ingest)})))

(defn get-readable-article [article]
  (json/read-str
   (@(http/get (wolfcastle-url (:article/original_url article))) :body)
   :key-fn keyword))

(defn update-original-html [article]
  (db/t [{:db/id (:db/id article)
          :article/original_html (fetch-original-html article)
          :article/ingest_state "fetched"}]))

(defn update-readable-html [article]
  (let [readable-article (get-readable-article article)]
    (db/t [{:db/id (:db/id article)
            :article/title (readable-article :title)
            :article/readable_body (readable-article :body)
            :article/ingest_state "ingested"}])))

(defn article-ingest [guid]
  (let [article (find-one-article-by-guid guid)]
    (if (and (update-original-html article)
             (update-readable-html article))
      {:status :success}
      {:status :error})))

(def article-ingest-worker
  (car-mq/worker {:pool {} :spec {:uri redis-uri}}
                 "article-ingest-queue"
                 {:handler (fn [{:keys [message]}]
                             (info "Article Ingest Worker got work:" message)
                             (article-ingest message))
                  ;; enable to log dry runs on each tick:
                  ;; :monitor (fn [{:keys [mid-circle-size ndry-runs poll-reply]}] (info "dry runs:" ndry-runs))
                  :throttle-ms 500
                  :eoq-backoff-ms 100}))

(defn enqueue-article-ingest [article-guid]
  (wcar* (car-mq/enqueue "article-ingest-queue" article-guid)))
