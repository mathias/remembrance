(ns remembrance.core
  (:require [remembrance.database :refer [prepare-database! db]]
            [remembrance.routes.core :as routes]
            [remembrance.routes.api :as api]
            [remembrance.views :refer [index-page]]
            [remembrance.workers :refer [ping-redis]]
            [playnice.core :refer [dassoc] :as playnice]
            [ring.middleware.params :refer [wrap-params]]
            [taoensso.timbre :refer [info]]))

(def routes (atom {}))

(defn route [path destination]
  (swap! routes
         dassoc
         path
         destination))

(defn wire-app-routes []
  (route "/" (fn [req] {:body (index-page)}))
  ;; API resources
  (route "/api/articles" api/article-routes)
  (route "/api/notes" api/note-routes))

(defn remembrance-init []
  (info "Migrations:" (prepare-database!))
  (info "DB:" (db))
  (info "Redis PING:" (ping-redis))
  (wire-app-routes)
  (info "Routes:" @routes))

(defn routes-handler [req]
  (playnice/dispatch @routes req))

(def remembrance-handler
  (->
   routes-handler
   (wrap-params)))
