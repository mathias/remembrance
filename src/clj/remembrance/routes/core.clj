(ns remembrance.routes.core
  (:require [cheshire.core :as json]))

(defn respond-with
  ([body] {:body body})
  ([body status] {:status status :body body})
  ([body status headers] {:status status :headers headers :body body}))

(defn respond-with-error []
  (respond-with {:ok false :errors "Unproccessable Entity."} 422))

(defn respond-with-json [body]
  (respond-with (json/generate-string body {:pretty true}) 200 {"Content-Type" "application/json"}))

(defn string-keys-to-symbols [map]
    (reduce #(assoc %1 (-> (key %2) keyword) (val %2)) {} map))

(defn keyword-form-params [ctx]
  (string-keys-to-symbols (get-in ctx [:request :form-params])))
