(ns remembrance.core
  (:require [cheshire.core :refer [generate-string]]
            [environ.core :refer [env]]
            [liberator.core :refer [defresource]]
            [playnice.core :refer [dassoc] :as playnice]
            [ring.middleware.params :refer [wrap-params]]))

(defn respond-with
  ([body] {:body body})
  ([body status] {:status status
                  :body body})
  ([body status headers] {:status status
                          :headers headers
                          :body body}))

(def resource-defaults
  {:available-media-types ["application/vnd.remembrance+json"]
   :handle-not-found (fn [_] {:errors ["Resource not found."]})
   :handle-not-acceptable (fn [_] {:errors ["Request type not acceptable."]})
   :handle-not-implemented (fn [_] {:errors ["Not implemented."]})})

(defn jsonify [response]
  (generate-string response {:pretty true}))

(defn url-to [path]
  (str (env :hostname) ":" (env :port) path))

(defn api-url-to [path]
  (url-to (str "/api" path)))

(def api-index-response
  {:current_user_url (api-url-to "/user")
   :articles_url (api-url-to "/articles")
   :articles_import_url (api-url-to "/articles/import")
   :articles_search_url (api-url-to "/articles/search")
   :notes_url (api-url-to "/notes")
   :stats_url (api-url-to "/stats")})

(defresource api-index
  resource-defaults
  :allowed-methods [:get]
  :handle-ok (fn [_] (jsonify api-index-response)))

(defresource articles-route
  resource-defaults
  :allowed-methods [:get]
  :handle-ok (fn [_] (jsonify {:articles []})))

(def routes (atom {}))

(defn route [path destination]
  (swap! routes
         dassoc
         path
         destination))

(defn define-routes!
  []
  ;; API discovery endpoint
  (route "/api/" api-index)

  ;; Articles
  (route "/api/articles" articles-route))

;; we must do this in the namespace and not init fn below,
;; because ring in dev will reload this file but not re-run init,
;; losing all route definitions.

(define-routes!)

(defn routes-handler [req]
  (playnice/dispatch @routes req))

(def remembrance-handler
  (-> routes-handler
      (wrap-params)))
