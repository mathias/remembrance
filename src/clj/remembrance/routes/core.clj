(ns remembrance.routes.core
  (:require [clojure.walk :refer [keywordize-keys]]
            [liberator.core :refer [defresource]]
            [remembrance.models.article :refer [articles-stats]]
            [remembrance.models.note :refer [notes-stats]]
            [remembrance.utils.health :refer [health-stats]]))

(defn respond-with
  ([body] {:body body})
  ([body status] {:status status :body body})
  ([body status headers] {:status status :headers headers :body body}))

(defn keywordize-form-params [ctx]
  (keywordize-keys (get-in ctx [:request :form-params])))

(defn keywordize-query-params [ctx]
  (keywordize-keys (get-in ctx [:request :query-params])))

(defresource api-stats
  :available-media-types ["application/json"]
  :allowed-methods [:get]
  :handle-ok (fn [_] {:stats
                     {:articles (articles-stats)
                      :notes (notes-stats)}}))

(defresource api-health
  :available-media-types ["application/json"]
  :allowed-methods [:get]
  :handle-ok (fn [_] {:health (health-stats)}))
