(ns remembrance.core
  (:require [remembrance.database :refer [prepare-database! db]]
            [remembrance.routes.core :refer [respond-with]]
            [remembrance.routes.articles :as articles]
            [remembrance.routes.notes :as notes]
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

;; Pages
(route "/" (fn [req] (respond-with (index-page))))

;; API
(route "/api/articles" articles/index-path)
(route "/api/articles/:guid" articles/show-article)
(route "/api/articles/search" articles/search)
(route "/api/articles/stats" articles/stats)
(route "/api/notes" notes/index-path)

(defn remembrance-init []
  (info "Migrations:" (prepare-database!))
  (info "DB:" (db))
  (info "Redis PING:" (ping-redis))
  ;;(info "Routes:")
  ;;(clojure.pprint/pprint @routes)
  )

(defn routes-handler [req]
  (playnice/dispatch @routes req))

(def remembrance-handler
  (->
   routes-handler
   (wrap-params)))
