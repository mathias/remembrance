(ns remembrance.core
  (:require
   [cheshire.core :as json]
   [environ.core :refer [env]]
   [liberator.core :refer [defresource]]
   [playnice.core :refer [dassoc] :as playnice]
   [ring.middleware.params :refer [wrap-params]]
   [com.ashafa.clutch :as clutch]
   [clojure.walk :refer [keywordize-keys]]))


;; db stuff

(def db (assoc (cemerick.url/url (env :database-host) (env :database-name))
          :username (env :database-username)
          :password (env :database-password)))

;; Can't define views since we're not database admin:
;; (def all-articles-view
;;   {:all-articles {:map
;;                   "function(doc) { if (doc.type && doc.type == \"article\") { emit(doc._id, doc); }}"}})

;; (clutch/with-db (env :database-name)
;;   (clutch/save-view "articles"
;;                     [:javascript all-articles-view]))

;; resource stuff

(defn respond-with
  ([body] {:body body})
  ([body status] {:status status
                  :body body})
  ([body status headers] {:status status
                          :headers headers
                          :body body}))

(defn keywordize-json-body [ctx]
  (keywordize-keys (json/parse-string (slurp (get-in ctx [:request :body])))))

(defn keywordize-form-params [ctx]
  (keywordize-keys (get-in ctx [:request :form-params])))

(defn keywordize-query-params [ctx]
  (keywordize-keys (get-in ctx [:request :query-params])))

(def resource-defaults
  {:available-media-types ["application/vnd.remembrance+json"]
   :handle-not-found (fn [_] {:errors ["Resource not found."]})
   :handle-not-acceptable (fn [_] {:errors ["Request type not acceptable."]})
   :handle-not-implemented (fn [_] {:errors ["Not implemented."]})})

(defn jsonify [response]
  (json/generate-string response {:pretty true}))

;; url stuff

(def hostname-with-port (str (env :hostname) (when (env :port) (str ":" (env :port)))))

(defn api-url-to [path]
  (str hostname-with-port "/api/" path))

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

(defn all-articles []
  (clutch/get-view db "articles" "all-articles"))

(defn create-article [params]
  (when-let [original-url (:original-url params)]
    (println "Inside the thing!")
    (let [new-doc
          (clutch/put-document db {:original-url original-url
                                   :type "article"
                                   :ingest-state "new"})]
      (println new-doc)
      (:_id new-doc))))

(defresource articles-route
  resource-defaults
  :allowed-methods [:get :post]
  :handle-ok (fn [_] (jsonify {:articles (all-articles)}))
  :post! (fn [ctx]
           (dosync
            (println "Gonna make an article!")
            (let [params (keywordize-json-body ctx)
                  guid (create-article params)]
              (println params)
              {::guid guid})))
  :post-redirect? (fn [ctx]
                    {:location (api-url-to (str "/articles/" (::guid ctx)))}))

(defresource article-route
  resource-defaults
  :allowed-methods [:get]
  :exists? (fn [ctx]
             (if-let [article (clutch/get-document (get-in ctx [:request :guid]))]
               {::article article}))
  :handle-ok (fn [ctx]
               (jsonify {:articles [(get ctx ::article)]})))


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
  (route "/api/articles" articles-route)
  (route "/api/articles/:guid" article-route))

;; we must do this in the namespace and not init fn below,
;; because ring in dev will reload this file but not re-run init,
;; losing all route definitions.

(define-routes!)

(defn routes-handler [req]
  (playnice/dispatch @routes req))

(def remembrance-handler
  (-> routes-handler
      (wrap-params)))
