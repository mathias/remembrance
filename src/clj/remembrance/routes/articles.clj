(ns remembrance.routes.articles
  (:require [liberator.core :refer [defresource]]
            [cemerick.url :refer [url url-encode]]
            [remembrance.config :as config]
            [remembrance.models.article :as article]
            [remembrance.models.note :as note]
            [remembrance.routes.core :refer :all]
            [remembrance.workers :refer [enqueue-article-original-html]]
            [ring.util.response :refer [redirect]]
            [taoensso.timbre :refer [info]]))

(def env (config/load!))

(defn article-index-url []
  (str (assoc (url (env :hostname)) :path "/api/articles")))

(defn article-show-url [guid]
  (str (assoc (url (env :hostname)) :path (str "/api/articles/" guid))))

(defn article-wrap-json [article]
  {:href (article-show-url (:article/guid article))
   :guid (:article/guid article)
   :title (:article/title article)
   :original_url (:article/original_url article)
   :read (:article/read article)})

(defn full-article-wrap-json [full-article]
  {:href (article-show-url (:article/guid full-article))
   :guid (:article/guid full-article)
   :title (:article/title full-article)
   :original_url (:article/original_url full-article)
   :readable_body (:article/readable_body full-article)
   :read (:article/read full-article)})

(defn article-collection-json [collection]
  (map article-wrap-json collection))

(defn create-and-enqueue-article [params]
  (let [article (article/create-article params)
        guid (:article/guid article)]
    (enqueue-article-original-html guid)
    guid))

;;(defn article-routes []
  ;;  (GET "/" [] (respond-with-json {:articles (article-collection-json (article/find-all-ingested-articles))}))

;; (create-and-enqueue-article params)
  ;; (GET "/search" {:keys [params]} (respond-with-json (article-collection-json (article/search-articles (:q params)))))
  ;; (GET "/stats" [] (respond-with-json (article/articles-stats)))
  ;; (GET "/:guid" [guid] (let [article (article/show-article guid)]
  ;;                        (if-not (nil? article)
  ;;                          (respond-with-json (full-article-wrap-json article))
  ;;                          (respond-with-error)))))

(defresource index-path
  :available-media-types ["application/json" "application/x-www-form-urlencoded"]
  :allowed-methods [:get :post]
  :handle-ok (fn [_]
               {:articles (article-collection-json
                           (article/find-all-ingested-articles))})
  :post! (fn [ctx]
           (dosync
            (let [guid (create-and-enqueue-article (keywordize-form-params ctx))]
              {::guid guid})))
  :post-redirect? (fn [ctx]
                    {:location (article-show-url (::guid ctx))}))

(defresource show-article
  :available-media-types ["application/json"]
  :allowed-methods [:get]
  :exists? (fn [ctx]
             (if-let [article (article/show-article (get-in ctx [:request :guid]))]
               {::article article}))
  :handle-ok (fn [ctx]
               {:articles [(full-article-wrap-json (get ctx ::article))]}))

(defresource search
  :available-media-types ["application/json"]
  :allowed-methods [:get]
  :handle-ok (fn [ctx]
               (let [query (:q (keywordize-query-params ctx))
                     articles (article-collection-json (article/search-articles query))]
                 {:articles articles})))

(defresource stats
  :available-media-types ["application/json"]
  :allowed-methods [:get]
  :handle-ok (fn [_] {:stats {:articles (article/articles-stats)}}))
