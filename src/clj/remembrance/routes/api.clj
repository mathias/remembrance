(ns remembrance.routes.api
  (:require [cheshire.core :as json]
            [compojure.core :refer [GET POST context defroutes]]
            [remembrance.config :refer [load!]]
            [remembrance.models.article :as article]
            [remembrance.models.note :as note]
            [remembrance.workers :refer [enqueue-article-original-html]]
            [ring.util.response :refer [redirect]]))

(def env (load!))

(defn respond-with
  ([body] {:body body})
  ([body status] {:status status :body body})
  ([body status headers] {:status status :headers headers :body body}))

(defn respond-with-error []
  (respond-with {:ok false :errors "Unproccessable Entity."} 422))

(defn respond-with-json [body]
  (respond-with (json/generate-string body {:pretty true}) 200 {"Content-Type" "application/json"}))

(defn article-index-url []
  ;; TODO: Use cemerick.url to compose URLs
  (str (env :hostname) "/api/articles"))

(defn article-show-url [guid]
  ;; TODO: Use cemerick.url to compose URLs
  (str (env :hostname) "/api/articles/" guid))

(defn article-wrap-json [article]
  {:href (article-show-url (:article/guid article))
   :guid (:article/guid article)
   :title (:article/title article)
   :original_url (:article/original_url article)
   :read (:article/read article)
   })

(defn full-article-wrap-json [full-article]
  {:href (article-show-url (:article/guid full-article))
   :guid (:article/guid full-article)
   :title (:article/title full-article)
   :original_url (:article/original_url full-article)
   :readable_body (:article/readable_body full-article)
   :read (:article/read full-article)
   })

(defn article-collection-json [collection]
  (map article-wrap-json collection))

(defn create-and-enqueue-article [params]
  (let [article (article/create-article params)
        guid (:article/guid article)]
    (enqueue-article-original-html guid)
    (redirect (article-show-url guid))))

(defn note-show-url [guid]
  ;; TODO: Use cemerick.url to compose URLs
  (str (env :hostname) "/api/notes/" guid))

(defn note-wrap-json [note]
  {:href (note-show-url (:note/guid note))
   :guid (:note/guid note)
   :title (:note/title note)
   :articles (article-collection-json (:note/articles note))})

(defn note-collection-json [coll]
  (map note-wrap-json coll))


(defroutes article-routes
  (GET "/" [] (respond-with-json {:articles (article-collection-json (article/find-all-ingested-articles))}))
  (POST "/" {:keys [params]} (create-and-enqueue-article params))
  (GET "/search" {:keys [params]} (respond-with-json (article-collection-json (article/search-articles (:q params)))))
  (GET "/stats" [] (respond-with-json (article/articles-stats)))
  (GET "/:guid" [guid] (let [article (article/show-article guid)]
                         (if-not (nil? article)
                           (respond-with-json (full-article-wrap-json article))
                           (respond-with-error)))))

(defroutes note-routes
  (GET "/" [] (respond-with-json {:notes (note-collection-json (note/all-notes))})))

(defroutes api-routes
  (context "/articles" [] article-routes)
  (context "/notes" [] note-routes))
