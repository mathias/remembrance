(ns remembrance.core
  (:require [compojure.core :refer [GET context defroutes]]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.middleware.json :as json]
            [remembrance.api :refer :all]
            [remembrance.config :as config]
            [remembrance.views :refer [index-page]]
            [remembrance.config :refer :all]))

(def env (config/load!))

(defmacro wrap [resp] `{:body ~resp})

(defroutes api-routes
  (GET "/documents" [] (wrap (all-documents))))

(defroutes app-routes
  (GET "/" [] (index-page))

  ;; API resources
  (context "/api" [] api-routes)

  ; to serve static pages saved in resources/public directory
  (route/resources "/")

  ; if page is not found
  (route/not-found "Page not found."))

(def remembrance-handler
  (->
   (compojure.handler/site app-routes)
   (json/wrap-json-body)
   (json/wrap-json-params)
   (json/wrap-json-response)))
