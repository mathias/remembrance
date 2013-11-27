(ns remembrance.core
  (:require [remembrance.models.document :refer :all]
            [remembrance.db :as db]
            [remembrance.views :refer [index-page]]
            [compojure.core :refer :all]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.middleware.params :refer [wrap-params]]
            [ring.util.response :refer [response content-type]]
            [ring.middleware.json :as json]
            [taoensso.timbre :refer [info]]))

(defmacro wrap [resp] `{:body ~resp})

(defroutes api-routes
  (context "/documents" []
           (defroutes documents-routes
             (GET "/" [] (wrap (all-documents)))
             (POST "/" {:keys [params]} (wrap (create-document params)))
             (GET "/:id" [id] (wrap (show-document id)))))
  (context "/notes" []
           (defroutes notes-routes
             (GET "/" [] (wrap "foo")))))

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
