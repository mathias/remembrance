(ns remembrance.acceptance-test
  (:require [midje.sweet :refer :all]
            [schema.core :as s]
            [remembrance.test-support.acceptance-helpers :refer :all]
            [remembrance.routes.response-schemas :refer :all]
            [remembrance.test-support.database :refer :all]))


(defn get-notes-list [json-body]
  (get json-body "notes"))

(defn create-initial-note []
  (post! "/api/notes" {:title "My note"
                       :body "Body"}))

(defn existing-note-guid []
  (-> (create-initial-note)
      (parse-redirect-location)
      (get-guid-from-uri)))

(background (around :facts (with-redefs [remembrance.database/connection (prepare-conn-with-existing-article)] ?form)))

(facts "Endpoints are all accessible"
       (fact "GET / (index-page)"
             (:body (get! "/")) => not-empty)

       (fact "GET /api/notes"
             (get! "/api/notes") => ok?)

       (fact "POST /api/notes"
             (post! "/api/notes" {:title "My note"}) => redirects?)

       (fact "GET /api/notes/:guid"
             (let [existing-note-guid (existing-note-guid)]
               (get! (str "/api/notes/" existing-note-guid))) => ok?)

       (fact "PUT /api/notes/:guid"
             (let [existing-note-guid (existing-note-guid)]
               (put! (str "/api/notes/" existing-note-guid)
                     {"body" "Body text"}))
             => ok?)

       (fact "GET /api/notes/stats"
             (get! "/api/notes/stats") => ok?)

       (fact "GET /api/stats"
             (get! "/api/stats") => ok?)

       (fact "GET /api/health"
             (get! "/api/health") => ok?))

(facts "JSON schema structure"
       (fact "GET /api/notes"
             (get-parsed-json-body "/api/notes") => (schema-valid? NoteList))

       (fact "Check schema of one item in /api/notes"
             (-> (get-parsed-json-body "/api/notes")
                 (:notes)
                 (first))
             =>
             (schema-valid? NoteInfo))

       (fact "GET /api/notes/:guid"
             (let [existing-note-guid (existing-note-guid)]
               (-> existing-note-guid
                   (str "/api/notes")
                   (get-parsed-json-body)
                   (:notes)
                   (first)))
             =>
             (schema-valid? NoteInfo))

       (fact "PUT /api/notes/:guid"
             (let [existing-note-guid (existing-note-guid)]
               (-> (put! (str "/api/notes/" existing-note-guid)
                         {:title "Foo" :body "Body"})
                   (parsed-json-body)
                   (:notes)
                   (first)))
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
