(ns remembrance.routes.notes
  (:require [remembrance.routes.core :refer [respond-with]]
            [remembrance.models.note :as note]
            [liberator.core :refer [defresource]]))

(defresource index-path
  :handle-ok (fn [_] (note/all-notes)))

(defn note-routes []
  ;; (GET "/" [] (respond-with-json {:notes (note-collection-json (note/all-notes))}))
  ;; (POST "/" {:keys [params]} (create-note-and-redirect params))))
  )
