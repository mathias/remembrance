(ns remembrance.routes.notes
  (:require [cemerick.url :refer [url]]
            [remembrance.config :as config]
            [remembrance.routes.core :refer :all]
            [remembrance.models.note :as note]
            [liberator.core :refer [defresource]]
            [taoensso.timbre :refer [info]]))

(def env (config/load!))

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
  :available-media-types ["application/json"]
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
