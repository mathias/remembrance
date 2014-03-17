(ns remembrance.routes.articles
  (:require [liberator.core :refer [defresource]]
            [cemerick.url :refer [url url-encode]]
            [schema.coerce :as coerce]
            [remembrance.config :refer [env]]
            [remembrance.models.article :as article]
            [remembrance.models.note :as note]
            [remembrance.routes.core :refer :all]
            [remembrance.routes.response-schemas :refer :all]
            [ring.util.response :refer [redirect]]))

(defn article-show-url [guid]
  (str (assoc (url (env :hostname)) :path (str "/api/articles/" guid))))

(def coerce-article-response
  (coerce/coercer ArticleInfo coerce/json-coercion-matcher))

(def coerce-full-article-response
  (coerce/coercer FullArticle coerce/json-coercion-matcher))

(defn article-wrap-json [article]
  (coerce-article-response {:href (article-show-url (:article/guid article))
                            :guid (:article/guid article)
                            :title (:article/title article)
                            :original_url (:article/original_url article)
                            :read (or (:article/read article)
                                      false)}))

(defn full-article-wrap-json [full-article]
  (coerce-full-article-response
   {:href (article-show-url (:article/guid full-article))
    :guid (:article/guid full-article)
    :title (:article/title full-article)
    :original_url (:article/original_url full-article)
    :readable_body (or (:article/readable_body full-article)
                       "")
    :read (or (:article/read full-article)
              false)}))

(defn article-collection-json [collection]
  (map article-wrap-json collection))

(defn articles-stats-json []
  (let [db (remembrance.database/db)]
    {:total (article/count-all-articles db)
     :ingested (article/count-articles-with-ingest-state db :article.ingest_state/ingested)
     :fetched (article/count-articles-with-ingest-state db :article.ingest_state/fetched)
     :errored (article/count-articles-with-ingest-state db :article.ingest_state/errored)
     :read (article/count-read-articles db)}))

(defn create-and-enqueue-article [params]
  (let [article (article/create-article params)
        guid (:article/guid article)]
    ;; TODO: enqueue article ingestion here
    ;; (enqueue-article-original-html guid)
    guid))

(defresource index-path
  resource-defaults
  :available-media-types ["application/json" "application/x-www-form-urlencoded"]
  :allowed-methods [:get :post]
  :handle-ok (fn [_]
               (jsonify {:articles (article-collection-json
                                    (article/find-all-ingested-articles))}))
  :post! (fn [ctx]
           (dosync
            (let [guid (create-and-enqueue-article (keywordize-form-params ctx))]
              {::guid guid})))
  :post-redirect? (fn [ctx]
                    {:location (article-show-url (::guid ctx))}))

(defresource show-article
  resource-defaults
  :available-media-types ["application/json" "application/x-www-form-urlencoded"]
  :allowed-methods [:get :put]

  :exists? (fn [ctx]
             (if-let [article (article/find-article-by-guid (get-in ctx [:request :guid]))]
               {::article article}))
  :handle-ok (fn [ctx]
               (jsonify {:articles [(full-article-wrap-json (get ctx ::article))]}))
  :can-put-to-missing? false
  :new? false
  :respond-with-entity? true
  :put! (fn [ctx]
          (dosync
           (let [article (get ctx ::article)
                 attributes (keywordize-form-params ctx)]
             (article/update-article remembrance.database/connection article attributes)))))

(defresource search
  resource-defaults
  :available-media-types ["application/json"]
  :allowed-methods [:get]
  :handle-ok (fn [ctx]
               (let [query (:q (keywordize-query-params ctx))
                     articles (article-collection-json (article/search-articles query))]
                 (jsonify {:articles articles}))))

(defresource stats
  resource-defaults
  :available-media-types ["application/json"]
  :allowed-methods [:get]
  :handle-ok (fn [_] (jsonify {:stats {:articles (articles-stats-json)}})))
