(ns remembrance.acceptance-test
  (:require [midje.sweet :refer :all]
            [ring.mock.request :refer :all]
            [clojure.data.json :as json]
            [schema.core :as s]
            [remembrance.test-support.schemas :refer :all]
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
  (if (= :post (:request-method request))
    (content-type request "application/x-www-form-urlencoded")
    request))

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

(def existing-article-uri "http://example.com")

(defn create-initial-article [uri]
  (post! "/api/articles" {"original_url" uri}))

(defonce existing-article-guid
  (-> (create-initial-article existing-article-uri)
      (parse-redirect-location)
      (get-guid-from-uri)))

(defn get-notes-list [json-body]
  (get json-body "notes"))

(defn get-href [json-body]
  (get json-body "href"))

(defonce existing-note-guid
  (-> (get! "/api/notes")
      (get-body)
      (json/read-str)
      (get-notes-list)
      (first)
      (get-href)
      (get-guid-from-uri)))

(facts "Endpoints are all accessible"
       (fact "GET / (index-page)"
             (:body (get! "/")) => not-empty)

       ;; JSON endpoints:
       (fact "GET /api/articles"
             (get! "/api/articles") => ok?)

       (fact "POST /api/articles with article URL"
             (post! "/api/articles"
                    {"original_url" existing-article-uri})
             =>
             redirects?
             (provided
              (remembrance.workers/enqueue-article-original-html & anything)
              =>
              true))

       (fact "GET /api/articles/:guid"
             (get! (str "/api/articles/" existing-article-guid)) => ok?)

       (fact "PUT /api/articles/:guid"
             (put! (str "/api/articles/" existing-article-guid)
                   {"read" true})
             =>
             ok?)

       (fact "GET /api/articles/search"
             (get! "/api/articles/search?q=Example") => ok?)

       (fact "GET /api/articles/stats"
             (get! "/api/articles/stats") => ok?)

       (fact "GET /api/notes"
             (get! "/api/notes") => ok?)

       (fact "POST /api/notes"
             (post! "/api/notes" {:title "My note"}) => redirects?)

       (fact "GET /api/notes/:guid"
             (get! (str "/api/notes/" existing-note-guid)) => ok?)

       (fact "PUT /api/notes/:guid"
             (put! (str "/api/notes/" existing-note-guid)
                   {"body" "Body text"})
             => ok?)

       (fact "GET /api/notes/stats"
             (get! "/api/notes/stats") => ok?)

       (fact "GET /api/stats"
             (get! "/api/stats") => ok?)

       (fact "GET /api/health"
             (get! "/api/health") => ok?))

(defn parsed-json-body [resp]
  (-> resp
      (get-body)
      (json/read-str)
      (clojure.walk/keywordize-keys)))

(defn get-parsed-json-body [route]
  (-> (get! route)
      (parsed-json-body)))

(facts "JSON schema structure"
       (fact "GET /api/articles"
             (get-parsed-json-body "/api/articles") => (schema-valid? ArticleList))

       (fact "Check schema of one item in /api/articles"
             (first (:articles (get-parsed-json-body "/api/articles")))
             =>
             (schema-valid? ArticleInfo))

       (fact "GET /api/articles/:guid"
             (get-parsed-json-body (str "/api/articles/" existing-article-guid))
             =>
             (schema-valid? FullArticle))

       (fact "PUT /api/articles/:guid"
             (parsed-json-body (put! (str "/api/articles/" existing-article-guid)
                                      {"read" true}))
             =>
             (schema-valid? FullArticle))

       (fact "GET /api/articles/search"
             (get-parsed-json-body "/api/articles/search?q=Example")
             =>
             (schema-valid? ArticleList))

       (fact "GET /api/articles/stats"
             (get-in (get-parsed-json-body "/api/articles/stats") [:stats :articles])
             =>
             (schema-valid? ArticleStats))

       (fact "GET /api/notes"
             (get-parsed-json-body "/api/notes") => (schema-valid? NoteList))

       (fact "Check schema of one item in /api/notes"
             (first (:notes (get-parsed-json-body "/api/notes")))
             =>
             (schema-valid? NoteInfo))

       (fact "GET /api/notes/:guid"
             (first (:notes (get-parsed-json-body (str "/api/notes/" existing-note-guid))))
             =>
             (schema-valid? NoteInfo))

       (fact "PUT /api/notes/:guid"
             (first (:notes
                     (parsed-json-body
                      (put! (str "/api/notes/" existing-note-guid)
                            {"body" "Body text"}))))
             => (schema-valid? NoteInfo))

       (fact "GET /api/notes/stats"
             (get-in (get-parsed-json-body "/api/notes/stats") [:stats :notes])
             =>
             (schema-valid? NoteStats))

       (fact "GET /api/stats"
             (get-parsed-json-body "/api/stats")
             =>
             (schema-valid? ApiStats))

       (fact "GET /api/health"
             (get-parsed-json-body "/api/health")
             =>
             (schema-valid? ApiHealth)))