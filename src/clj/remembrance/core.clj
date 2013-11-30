(ns remembrance.core
  (:require [remembrance.models.article :refer :all]
            [remembrance.db :as db]
            [remembrance.views :refer [index-page]]
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

(defn show-article-url [doc-id]
  (str (env :hostname) "/api/articles/" doc-id))

(defroutes api-routes
  (context "/articles" []
           (defroutes articles-routes
             (GET "/" [] (respond-with (all-articles)))
             (POST "/" {:keys [params]} (let [doc (create-article params)]
                                          (if-not (some false? doc)
                                            (redirect (show-article-url doc))
                                            (respond-with-error))))
             (GET "/:id" [id] (let [article (show-article id)]
                                (if-not (false? article)
                                  (respond-with article)
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
  (info "DB:" (db/prepare!)))


(def remembrance-handler
  (->
   (compojure.handler/site app-routes)
   (wrap-params)
   (json/wrap-json-body)
   (json/wrap-json-params)
   (json/wrap-json-response)))
