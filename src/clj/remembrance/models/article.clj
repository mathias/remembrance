(ns remembrance.models.article
  (require [remembrance.db :as db]
           [datomic.api :as d]
           [taoensso.timbre :refer [info]]))

(defn show-article []
  {})

(defn create-article [attrs]
  ;; (d/transact db/connection
  ;;             [["db/add" (d/tempid "db.part/article")  ]]))
  nil)

(defn all-articles []
  (d/q '[:find ?a :where [?a :article/title]] (d/db db/connection)))
