(ns remembrance.test-support.acceptance-helpers
  (:require [midje.sweet :refer :all]
            [ring.mock.request :refer :all]
            [cheshire.core :as json]
            [remembrance.test-support.schemas :refer :all]
            [remembrance.test-support.database :refer :all]
            [remembrance.core :refer :all]))

(defn parse-redirect-location [response]
  (get-in response [:headers "Location"]))

(defn get-guid-from-uri [uri]
  (last (clojure.string/split (or uri "") #"/")))

(defn get-body [response]
  (:body response))

(defn follow-redirect [response]
  (request :get (parse-redirect-location response)))

(defn get! [route]
  (-> (request :get route)
      (remembrance-handler)))

(defn set-post-content-type [request]
  (content-type request "application/x-www-form-urlencoded")
  request)

(defn post! [route params]
  (-> (request :post route params)
      (set-post-content-type)
      (remembrance-handler)))

(defn put! [route params]
  (-> (request :put route params)
      (set-post-content-type)
      (remembrance-handler)))

(defn status? [expected response]
  (= expected
     (:status response)))

(defn ok? [response]
  (status? 200 response))

(defn redirects? [response]
  (or (status? 302 response)
      (status? 303 response)))

(defn get-href [json-body]
  (get json-body "href"))


(defn parsed-json-body [resp]
  (-> resp
      (get-body)
      (json/parse-string)
      (clojure.walk/keywordize-keys)))

(defn get-parsed-json-body [route]
  (-> (get! route)
      (parsed-json-body)))
