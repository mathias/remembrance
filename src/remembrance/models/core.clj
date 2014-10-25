(ns remembrance.models.core
  (:require [datomic.api :as d]))

(defn first-entity [db datomic-eid-vec]
  (d/entity db (first datomic-eid-vec)))
