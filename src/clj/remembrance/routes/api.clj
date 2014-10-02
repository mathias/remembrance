(ns remembrance.routes.api
  (:require [liberator.core :refer [defresource]]
            [remembrance.routes.core :refer :all]
            [remembrance.routes.articles :refer [articles-stats-json]]
            [remembrance.routes.route-helpers :refer :all]
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

(defresource api-map
  resource-defaults
  :available-media-types ["application/json"]
  :allowed-methods [:get]
  :handle-ok (fn [_]
               (jsonify {:articles_url articles-index-url
                         :article_url article-show-uri-template
                         :articles_search_url articles-search-uri-template
                         :articles_stats_url articles-stats-url
                         :notes_url notes-index-url
                         :note_url note-show-uri-template
                         :notes_search_url notes-search-uri-template
                         :api_stats_url api-stats-url
                         :api_health_url api-health-url})))
