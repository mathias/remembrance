(ns remembrance.routes.api
  (:require [liberator.core :refer [defresource]]
            [remembrance.routes.articles :refer [articles-stats-json]]
            [remembrance.models.note :refer [notes-stats]]
            [remembrance.utils.health :refer [health-stats]]))

(defresource stats
  :available-media-types ["application/json"]
  :allowed-methods [:get]
  :handle-ok (fn [_] {:stats
                     {:articles (articles-stats-json)
                      :notes (notes-stats)}}))

(defresource health
  :available-media-types ["application/json"]
  :allowed-methods [:get]
  :handle-ok (fn [_] {:health (health-stats)}))
