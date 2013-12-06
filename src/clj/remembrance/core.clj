(ns remembrance.core
  (:require [remembrance.models.article :as article]
            [remembrance.db :as db]
            [remembrance.views :refer [index-page]]
            [remembrance.workers :refer [ping-redis enqueue-article-ingest]]
            [compojure.core :refer :all]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.middleware.params :refer [wrap-params]]
            [ring.util.response :refer [response redirect content-type]]
            [ring.middleware.json :as json]
            [taoensso.timbre :refer [info]]))

(def env (remembrance.config/load!))

(defn respond-with
  ([body] {:body body})
  ([body status] {:status status :body body})
  ([body status headers] {:status status :headers headers :body body}))

(defn respond-with-error []
  (respond-with {:ok false :errors "Unproccessable Entity."} 422))

(defn article-index-url []
  (str (env :hostname) "/api/articles"))

(defn article-show-url [guid]
  (str (env :hostname) "/api/articles/" guid))

(defn article-wrap-json [article]
  {
   :href (article-show-url (:guid article))
   :guid (:guid article)
   :title (:title article)
   :original_url (:original_url article)
   })

(defn full-article-wrap-json [full-article]
  {
   :href (article-show-url (:article/guid full-article))
   :guid (:article/guid full-article)
   :title (:article/title full-article)
   :original_url (:article/original_url full-article)
   :readable_body (:article/readable_body full-article)
   })

(defn article-collection-json [collection]
  { :collection {
    :version (env :api-version)
    :href (article-index-url)
    :items (map article-wrap-json collection)
  }})

(defroutes api-routes
  (context "/articles" []
           (defroutes articles-routes
             (GET "/" [] (respond-with (article-collection-json (article/find-all-ingested-articles))))
             (POST "/" {:keys [params]} (let [article (article/create-article params)
                                              guid (:article/guid article)]
                                          (enqueue-article-ingest guid)
                                          (redirect (article-show-url guid))))
             (GET "/search" {:keys [params]} (respond-with (article-collection-json (article/search-articles (:q params)))))
             ;; (PUT "/:guid/mark_as_read" [guid] (respond-with (mark-article-as-read guid)))
             (GET "/:guid" [guid] (let [article (article/show-article guid)]
                                (if-not (nil? article)
                                  (respond-with (full-article-wrap-json article))
                                  (respond-with-error))))))
  (context "/notes" []
           (defroutes notes-routes
             (GET "/" [] (respond-with "foo")))))

(defroutes app-routes
  (GET "/" [] (index-page))

  ;; API resources
  (context "/api" [] api-routes)

  ; to serve static pages saved in resources/public directory
  (route/resources "/")

  ; if page is not found
  (route/not-found "Page not found."))

(defn remembrance-init []
  (info "DB:" (db/prepare!))
  (info "Redis PING:" (ping-redis)))

(def remembrance-handler
  (->
   (compojure.handler/site app-routes)
   (wrap-params)
   (json/wrap-json-body)
   (json/wrap-json-params)
   (json/wrap-json-response)))
