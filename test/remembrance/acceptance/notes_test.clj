(ns remembrance.acceptance.notes-test
  (:require [midje.sweet :refer :all]
            [clojure.test :refer :all]
            [schema.core :as s]
            [remembrance.test-support.acceptance-helpers :refer :all]
            [remembrance.routes.response-schemas :refer :all]
            [remembrance.test-support.database :refer :all]))

(background
 (around :facts (with-redefs [remembrance.database/connection (prepare-conn-with-seed-data)] ?form)))

(deftest notes-endpoints-are-all-accessible
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

  (fact "GET /api/notes/search"
        (get! "/api/notes/search?q=Example") => ok?)

  (fact "GET /api/notes/stats"
        (get! "/api/notes/stats") => ok?)

  (fact "GET /api/stats"
        (get! "/api/stats") => ok?)

  (fact "GET /api/health"
        (get! "/api/health") => ok?))

(deftest json-schema-structure
  (fact "GET /api/notes"
        (get-parsed-json-body "/api/notes") => (schema-valid? NoteList))

  (fact "Check schema of one item in /api/notes"
        (-> (get-parsed-json-body "/api/notes")
            (:notes)
            (first))
        =>
        (schema-valid? NoteInfo))

  (fact "GET /api/notes/:guid for empty note does not blow up on validation"
        (-> (str "/api/notes/" empty-note-guid)
            (get-parsed-json-body)
            (:notes)
            (first))
        =>
        (schema-valid? NoteInfo))

  (fact "GET /api/notes/:guid"
        (-> existing-note-guid
            (str "/api/notes")
            (get-parsed-json-body)
            (:notes)
            (first))
        =>
        (schema-valid? NoteInfo))

  (fact "PUT /api/notes/:guid"
        (-> (put! (str "/api/notes/" existing-note-guid)
                  {:title "Foo" :body "Body"})
            (parsed-json-body)
            (:notes)
            (first))
        => (schema-valid? NoteInfo))

  (fact "GET /api/notes/search"
        (get-parsed-json-body "/api/notes/search?q=Example")
        =>
        (schema-valid? NoteList))

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
