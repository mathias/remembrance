(ns remembrance.core
  (:require [compojure.core :refer :all]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [remembrance.database :refer [prepare-database!]]
            [remembrance.routes.api :as api-routes]
            [remembrance.views :refer [index-page]]
            [remembrance.workers :refer [ping-redis]]
            [ring.middleware.params :refer [wrap-params]]
            [taoensso.timbre :refer [info]]))

(defroutes app-routes
  (GET "/" [] (index-page))

  ;; API resources
  (context "/api" [] api-routes/article-routes)

  ; to serve static pages saved in resources/public directory
  (route/resources "/")

  ; if page is not found
  (route/not-found "Page not found."))

(defn remembrance-init []
  (info "DB:" (prepare-database!))
  (info "Redis PING:" (ping-redis)))

(def remembrance-handler
  (->
   (handler/site app-routes)
   (wrap-params)))
