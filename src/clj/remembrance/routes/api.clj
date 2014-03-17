(ns remembrance.routes.api
  (:require [liberator.core :refer [defresource]]
            [remembrance.routes.core :refer :all]
            [remembrance.routes.articles :refer [articles-stats-json]]
            [remembrance.routes.notes :refer [notes-stats-json]]
            [remembrance.utils.health :refer [health-stats]]))

(defresource stats
  resource-defaults
  :available-media-types ["application/json"]
  :allowed-methods [:get]
  :handle-ok (fn [_] (jsonify {:stats
                              {:articles (articles-stats-json)
                               :notes (notes-stats-json)}})))

(defresource health
  resource-defaults
  :available-media-types ["application/json"]
  :allowed-methods [:get]
  :handle-ok (fn [_] (jsonify {:health (health-stats)})))
