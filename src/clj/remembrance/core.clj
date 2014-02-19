(ns remembrance.core
  (:require [remembrance.database :refer [prepare-database! db shutdown-database!]]
            [remembrance.routes.core :refer [respond-with api-stats api-health]]
            [remembrance.routes.articles :as articles]
            [remembrance.routes.notes :as notes]
            [remembrance.views :refer [index-page]]
            [playnice.core :refer [dassoc] :as playnice]
            [ring.middleware.params :refer [wrap-params]]
            [taoensso.timbre :refer [info]]))

(def routes (atom {}))

(defn route [path destination]
  (swap! routes
         dassoc
         path
         destination))

(defn define-routes! []
  ;; Pages
  (route "/" (fn [req] (respond-with (index-page))))

  ;; API
  (route "/api/articles" articles/index-path)
  (route "/api/articles/:guid" articles/show-article)
  (route "/api/articles/search" articles/search)
  (route "/api/articles/stats" articles/stats)
  (route "/api/notes" notes/index-path)
  (route "/api/notes/:guid" notes/show-note)
  (route "/api/notes/stats" notes/stats)
  (route "/api/stats" api-stats)

  ;; Test / development
  (route "/api/health" api-health))


;; we must do this in the namespace and not init fn below,
;; because ring in dev will reload this file but not re-run init,
;; losing all route definitions!
(define-routes!)


(defn remembrance-init []
  ;; Turn off tests when running the server in production:
  (if (= (System/getenv "RING_ENV") "production")
    (alter-var-root #'clojure.test/*load-tests* (constantly false)))


  (info "Migrations:" (prepare-database!))
  (info "DB:" (db))

  (comment
    (info "Routes:")
    (clojure.pprint/pprint @routes)))

(defn routes-handler [req]
  (playnice/dispatch @routes req))

(def remembrance-handler
  (->
   routes-handler
   (wrap-params)))

(defn remembrance-clean-shutdown []
  (info "Shutting down the database connection.")
  (shutdown-database!))
