(ns remembrance.routes.notes
  (:require [remembrance.routes.route-helpers :refer :all]
            [remembrance.routes.core :refer :all]
            [remembrance.models.note :as note]
            [remembrance.routes.articles :refer [article-collection-json]]
            [liberator.core :refer [defresource]]))

(defn notes-stats-json []
  {:total (note/count-notes)})

(defn note-wrap-json [note]
  {:href (note-show-url (:note/guid note))
   :guid (:note/guid note)
   :title (or (:note/title note) "")
   :body (or (:note/body note) "")
   :articles (article-collection-json (:note/articles note))})

(defn note-collection-json [coll]
  (map note-wrap-json coll))

(defresource index-path
  resource-defaults
  :available-media-types ["application/json"
                          "application/x-www-form-urlencoded"]
  :allowed-methods [:get :post]
  :handle-ok (fn [_]
               (jsonify {:notes (note-collection-json (note/find-all-notes))}))
  :post! (fn [ctx]
           (dosync
            (let [params (keywordize-form-params ctx)
                  note (note/create-note params)
                  guid (:note/guid note)]
              {::guid guid})))
  :post-redirect? (fn [ctx]
                    {:location (note-show-url (::guid ctx))}))

(defresource show-note
  resource-defaults
  :available-media-types ["application/json"
                          "application/x-www-form-urlencoded"]
  :allowed-methods [:get :put]
  :exists? (fn [ctx]
             (if-let [note (note/find-note-by-guid
                            (get-in ctx [:request :guid]))]
               {::note note}))
  :handle-ok (fn [ctx]
               (jsonify {:notes [(note-wrap-json (get ctx ::note))]}))
  :can-put-to-missing? false
  :new? false
  :respond-with-entity? true
  :put! (fn [ctx]
          (dosync
           (let [note (get ctx ::note)
                 attributes (keywordize-form-params ctx)]
             (note/update-note remembrance.database/connection
                               note
                               attributes)))))

(defresource search
  resource-defaults
  :available-media-types ["application/json"]
  :allowed-methods [:get]
  :handle-ok (fn [ctx]
               (let [query (:q (keywordize-query-params ctx))
                     notes (note-collection-json (note/search-notes query))]
                 (jsonify {:notes notes}))))

(defresource stats
  resource-defaults
  :available-media-types ["application/json"]
  :allowed-methods [:get]
  :handle-ok (fn [_] (jsonify {:stats
                              {:notes (notes-stats-json)}})))
