(ns remembrance.models.document
  (require [remembrance.db :as db]
           [ring.util.response :refer [response redirect-after-post status]]
           [taoensso.timbre :refer [info]]))

(def table-name "documents")

(def document-fields [:title
                      :body
                      :original_url
                      :original_html
                      :date_published
                      :date_ingested
                      :created_at
                      :updated_at
                      :ingest_state
                      :read])

(def validate-presence-of [:original_url])

(def field-defaults {:read false
                     :ingest_state "new"})
