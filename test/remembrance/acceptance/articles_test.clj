(ns remembrance.acceptance.articles-test
  (:require [midje.sweet :refer :all]
            [remembrance.test-support.acceptance-helpers :refer :all]
            [remembrance.routes.response-schemas :refer :all]
            [remembrance.routes.request-schemas :refer :all]
            [remembrance.test-support.database :refer :all]))

(defn create-initial-article [uri]
  (post! "/api/articles" {"original_url" uri}))

(defn existing-article-guid []
  (-> (create-initial-article original-url)
      (parse-redirect-location)
      (get-guid-from-uri)))

(background
 (around :facts (with-redefs [remembrance.database/connection (prepare-conn-with-existing-article)] ?form)))

(facts "Articles endpoints are all accessible"
       (fact "GET /api/articles"
             (get! "/api/articles") => ok?)

       (fact "POST /api/articles with article URL"
             (post! "/api/articles"
                    {"original_url" original-url})
             =>
             redirects?
             (provided
              (remembrance.workers/enqueue-article-original-html & anything) => true))

       (fact "GET /api/articles/:guid"
             (let [guid (existing-article-guid)]
               (get! (str "/api/articles/" guid)))
             =>
             ok?
             (provided
              (remembrance.workers/enqueue-article-original-html & anything) => true))

       (fact "PUT /api/articles/:guid"
             (let [guid (existing-article-guid)]
               (put! (str "/api/articles/" guid)
                     {"read" true}))
             =>
             ok?
             (provided
              (remembrance.workers/enqueue-article-original-html & anything) => true))

       (fact "GET /api/articles/search"
             (get! "/api/articles/search?q=Example") => ok?)

       (fact "GET /api/articles/stats"
             (get! "/api/articles/stats") => ok?))

(facts "Article endpoints JSON schema structure"
       (fact "GET /api/articles"
             (get-parsed-json-body "/api/articles") => (schema-valid? ArticleList))

       (fact "Check schema of one item in /api/articles"
             (-> (get-parsed-json-body "/api/articles")
                 (:articles)
                 (first))
             =>
             (schema-valid? ArticleInfo))

       (fact "GET /api/articles/:guid"
             (let [guid (existing-article-guid)]
               (get-parsed-json-body (str "/api/articles/" guid)))
             =>
             (schema-valid? FullArticleList)
             (provided
              (remembrance.workers/enqueue-article-original-html & anything) => true))

       (fact "GET /api/articles/:guid gets coerced from nil values to strings"
             (->>  (existing-article-guid)
                   (str "api/articles/")
                   (get-parsed-json-body)
                   (:readable_body))
             =not=>
             nil?
             (provided
              (remembrance.workers/enqueue-article-original-html & anything) => true))

       (fact "PUT /api/articles/:guid"
             (let [guid (existing-article-guid)]
               (parsed-json-body (put! (str "/api/articles/" guid)
                                       {"read" true})))
             =>
             (schema-valid? FullArticleList)
             (provided
               (remembrance.workers/enqueue-article-original-html & anything) => true))

       (fact "GET /api/articles/search"
             (get-parsed-json-body "/api/articles/search?q=Example")
             =>
             (schema-valid? ArticleList))

       (fact "GET /api/articles/stats"
             (get-in (get-parsed-json-body "/api/articles/stats") [:stats :articles])
             =>
             (schema-valid? ArticleStats)))
