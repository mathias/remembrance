(ns remembrance.api
  (require [remembrance.db :as db]))

(defn all-documents []
  (db/select-all "documents"))
