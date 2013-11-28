(ns remembrance.core
  (:require [remembrance.models.document :refer :all]
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

(defn show-document-url [doc-id]
  (str (env :hostname) "/api/documents/" doc-id))

(defroutes api-routes
  (context "/documents" []
           (defroutes documents-routes
             (GET "/" [] (respond-with (all-documents)))
             (POST "/" {:keys [params]} (let [doc (create-document params)]
                                          (if-not (false? doc)
                                            (redirect (show-document-url doc))
                                            (respond-with-error))))
             (GET "/:id" [id] (let [doc (show-document id)]
                                (if-not (false? doc)
                                  (respond-with doc)
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
  (info "DB:" (db/prepare-db!))
  (info "Tables:" (db/prepare-tables!)))

(def remembrance-handler
  (->
   (compojure.handler/site app-routes)
   (wrap-params)
   (json/wrap-json-body)
   (json/wrap-json-params)
   (json/wrap-json-response)))
