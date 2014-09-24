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
   :handle-not-implemented (fn [_] {:errors ["Not implemented."]})})

(defn jsonify [response]
  (generate-string response {:pretty true}))

(defn url-to [path]
  (str (env :hostname) path))

(def api-index-response
  {:current_user_url (url-to "/user")
   :authorizations_url (url-to "/authorizations")
   :articles_url (url-to "/articles")
   :articles_import_url (url-to "/articles/import")
   :articles_search_url (url-to "/articles/search")
   :notes_url (url-to "/notes")
   :stats_url (url-to "/stats")})

(defresource api-index
  resource-defaults
  :allowed-methods [:get]
  :handle-ok (fn [_] (jsonify api-index-response)))

(def routes (atom {}))

(defn route [path destination]
  (swap! routes
         dassoc
         path
         destination))

(defn define-routes!
  []
  ;; API discovery endpoint
  (route "/" api-index)

  ;; Articles
  )

;; we must do this in the namespace and not init fn below,
;; because ring in dev will reload this file but not re-run init,
;; losing all route definitions.
(define-routes!)

(defn remembrance-init
  []
  ;; Turn off tests in production:
  (when false ;; TODO: FIXME
     (alter-var-root #'clojure.test/*load-tests* (constantly false))))

(defn routes-handler [req]
  (playnice/dispatch @routes req))

(def remembrance-handler
  (-> routes-handler
      (wrap-params)))
