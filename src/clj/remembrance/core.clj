(ns remembrance.core
  (:require [remembrance.database :refer [prepare-database! db]]
            [remembrance.routes.core :refer [respond-with]]
            [remembrance.routes.api :as api]
            [remembrance.routes.articles :as articles]
            [remembrance.routes.notes :as notes]
            [remembrance.views :refer [index-page]]
            [playnice.core :refer [dassoc] :as playnice]
            [ring.middleware.params :refer [wrap-params]]
            [environ.core :refer [env]]
            [taoensso.timbre :refer [info]]))

(defn production-env? []
  (memoize (= (env :ring-env) "production")))

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
  (route "/api/" api/api-map)
  (route "/api/articles" articles/index-path)
  (route "/api/articles/:guid" articles/show-article)
  (route "/api/articles/import" articles/import-articles)
  (route "/api/articles/search" articles/search)
  (route "/api/articles/stats" articles/stats)
  (route "/api/notes" notes/index-path)
  (route "/api/notes/:guid" notes/show-note)
  (route "/api/notes/search" notes/search)
  (route "/api/notes/stats" notes/stats)
  (route "/api/stats" api/stats)

  ;; Test / development
  (route "/api/health" api/health))

;; we must do this in the namespace and not init fn below,
;; because ring in dev will reload this file but not re-run init,
;; losing all route definitions!
(define-routes!)

(defn remembrance-init []
  ;; Turn off tests when running the server in production:
  (when (production-env?)
    (alter-var-root #'clojure.test/*load-tests* (constantly false)))

  (prepare-database!)
  (info "Migrations:" remembrance.database/migration-filenames)
  (info "DB:" (db))

  (when-not (production-env?)
    (info "Routes:")
    (clojure.pprint/pprint @routes)))

(defn routes-handler [req]
  (playnice/dispatch @routes req))

(def remembrance-handler
  (->
   routes-handler
   (wrap-params)))
