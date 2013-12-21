(ns remembrance.core
  (:require [compojure.core :refer :all]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [remembrance.database :as db]
            [remembrance.routes.api :as api-routes]
            [remembrance.views :refer [index-page]]
            [ring.middleware.params :refer [wrap-params]]
            [taoensso.timbre :refer [info]]))

(defroutes app-routes
  (GET "/" [] (index-page))

  ;; API resources
  (context "/api" [] api-routes/api-routes)

  ; to serve static pages saved in resources/public directory
  (route/resources "/")

  ; if page is not found
  (route/not-found "Page not found."))

(defn remembrance-init []
  (info "DB:" (db/prepare!))
  (info "Redis PING:" (ping-redis)))

(def remembrance-handler
  (->
   (handler/site app-routes)
   (wrap-params)))
