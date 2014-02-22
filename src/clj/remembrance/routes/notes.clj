(ns remembrance.routes.notes
  (:require [cemerick.url :refer [url]]
            [remembrance.config :refer [env]]
            [remembrance.routes.core :refer :all]
            [remembrance.models.note :as note]
            [liberator.core :refer [defresource]]
            [taoensso.timbre :refer [info]]))

(defn note-show-url [guid]
  (str (assoc (url (env :hostname)) :path (str "/api/notes/" guid))))

(defn note-wrap-json [note]
  {:href (note-show-url (:note/guid note))
   :guid (:note/guid note)
   :title (:note/title note)
   :body (:note/body note)
   :articles (remembrance.routes.articles/article-collection-json (:note/articles note))})

(defn note-collection-json [coll]
  (map note-wrap-json coll))

(defresource index-path
  :available-media-types ["application/json" "application/x-www-form-urlencoded"]
  :allowed-methods [:get :post]
  :handle-ok (fn [_]
               {:notes (note-collection-json (note/all-notes))})
  :post! (fn [ctx]
           (dosync
            (let [params (keywordize-form-params ctx)
                  note (note/create-note params)
                  guid (:note/guid note)]
              {::guid guid})))
  :post-redirect? (fn [ctx]
                    {:location (note-show-url (::guid ctx))}))

(defresource show-note
  :available-media-types ["application/json" "application/x-www-form-urlencoded"]
  :allowed-methods [:get :put]
  :exists? (fn [ctx]
             (if-let [note (note/show-note (get-in ctx [:request :guid]))]
               {::note note}))
  :handle-ok (fn [ctx]
               {:notes [(note-wrap-json (get ctx ::note))]})
  :can-put-to-missing? false
  :new? false
  :respond-with-entity? true
  :put! (fn [ctx]
          (dosync
           (let [note (get ctx ::note)
                 attributes (keywordize-form-params ctx)]
             (note/update-note note attributes)))))

(defresource stats
  :available-media-types ["application/json"]
  :allowed-methods [:get]
  :handle-ok (fn [_] {:stats {:notes (note/notes-stats)}}))
