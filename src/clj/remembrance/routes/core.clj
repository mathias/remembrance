(ns remembrance.routes.core
  (:require [clojure.walk :refer [keywordize-keys]]))

(defn respond-with
  ([body] {:body body})
  ([body status] {:status status :body body})
  ([body status headers] {:status status :headers headers :body body}))

(defn keywordize-form-params [ctx]
  (keywordize-keys (get-in ctx [:request :form-params])))

(defn keywordize-query-params [ctx]
  (keywordize-keys (get-in ctx [:request :query-params])))
