(ns remembrance.models.article-test
  (:require [midje.sweet :refer :all]
            [clojure.test :refer :all]
            [datomic.api :as d]
            [remembrance.test-support.database :refer :all]
            [remembrance.models.core :refer [first-entity]]
            [remembrance.models.article :refer :all]))

(deftest ensure-seed-transaction-works
  (fact "ensure our seed transaction works"
    (let [our-conn (prepare-conn-with-existing-article)
          db (d/db our-conn)
          txn-count (ffirst (d/q '[:find (count ?tx)
                                   :in $
                                   :where [?tx :db/txInstant _]]
                                 db))]
      (> txn-count 3))
    =>
    truthy))

(deftest find-article-by-guid-q-fn-test
  (fact "finds an existing article"
    (let [our-conn (prepare-conn-with-existing-article)
          db (d/db our-conn)]
      (find-article-by-guid-q db existing-guid))
    =not=>
    empty?)

  (fact "found article has correct attributes"
    (let [our-conn (prepare-conn-with-existing-article)
          db (d/db our-conn)
          eid (first (find-article-by-guid-q db existing-guid))]
      (:article/original_url (first-entity db eid)))
    =>
    original-url)

  (fact "finding an entity that doesn't exist"
    (let [our-conn (prepare-conn-with-existing-article)
          db (d/db our-conn)]
      (find-article-by-guid-q db "made-up-guid"))
    =>
    empty?))

(deftest find-article-by-guid-fn-test
  (fact "turns the first result entity ID into an entity"
    (let [our-conn (prepare-conn-with-existing-article)
          db (d/db our-conn)
          article (find-article-by-guid db existing-guid)]
      (:article/original_url article))
    =>
    original-url))

(deftest find-article-by-original-url-q-fn-test
  (fact "finds an existing article"
    (let [our-conn (prepare-conn-with-existing-article)
          db (d/db our-conn)]
      (find-article-by-original-url-q db original-url))
    =not=>
    empty?)

  (fact "found article has correct attributes"
    (let [our-conn (prepare-conn-with-existing-article)
          db (d/db our-conn)]
      (->>
       (find-article-by-original-url-q db original-url)
       (first)
       (first-entity db)
       (:article/original_url)))
    =>
    original-url)

  (fact "finding an entity that doesn't exist"
    (let [our-conn (prepare-conn-with-existing-article)
          db (d/db our-conn)]
      (find-article-by-original-url-q db "http://nonexistent-url.com"))
    =>
    empty?))

(deftest find-article-by-original-url-fn-test
  (fact "turns the first result entity ID into an entity"
    (let [our-conn (prepare-conn-with-existing-article)
          db (d/db our-conn)]
      (->
       (find-article-by-original-url db original-url)
       (:article/original_url)))
    =>
    original-url))

(deftest search-articles-q-fn-test
  (fact "finds an article which matches the search query"
    (let [our-conn (prepare-conn-with-existing-article)
          db (d/db our-conn)]
      (search-articles-q db "Example"))
    =not=>
    empty?)

  (fact "article that does not match query is not found"
    (let [our-conn (prepare-conn-with-existing-article)
          db (d/db our-conn)]
      (search-articles-q db "Not matching"))
    =>
    empty?))

(deftest search-articles-fn-test
  (fact "maps returned list of entity ids into entities (can get attributes)"
    (let [our-conn (prepare-conn-with-existing-article)
          db (d/db our-conn)]
      (:article/original_url (first (search-articles db "Example"))))
    =>
    original-url))

(deftest find-all-ingested-articles-q-fn-test
  (fact "returns a set containing our existing article"
    (let [our-conn (prepare-conn-with-existing-article)
          db (d/db our-conn)]
      (find-all-ingested-articles-q db))
    =not=>
    empty?)

  (fact "found article has correct attributes"
    (let [our-conn (prepare-conn-with-existing-article)
          db (d/db our-conn)
          found-eids (find-all-ingested-articles-q db)
          first-found-eid (first found-eids)]
      (:article/original_url (first-entity db first-found-eid)))
    =>
    original-url)

  (fact "when no ingested articles exist, returns empty set"
    (let [our-conn (prepare-migrated-db-conn)
          db (d/db our-conn)]
      (find-all-ingested-articles-q db))
    =>
    empty?))

(deftest find-all-ingested-articles-fn-test
  (fact "returns entities for ingested articles"
    (let [our-conn (prepare-conn-with-existing-article)
          db (d/db our-conn)]
      (-> (find-all-ingested-articles db)
          (first)
          (:article/original_url)))
    =>
    original-url))

(deftest count-read-articles-q-fn-test
  (fact "returns empty set when no read articles exist"
    (let [our-conn (prepare-migrated-db-conn)
          db (d/db our-conn)]
      (count-read-articles-q db))
    =>
    empty?)

  (fact "returns the correct count (1) when a single read article exists"
    (let [our-conn (prepare-conn-with-read-article)
          db (d/db our-conn)]
      (ffirst (count-read-articles-q db)))
    =>
    1))

(deftest count-read-articles-fn-test
  (fact "returns 0 when there are none"
    (let [our-conn (prepare-migrated-db-conn)
          db (d/db our-conn)]
      (count-read-articles db))
    =>
    0)

  (fact "returns the count when one read article exists"
    (let [our-conn (prepare-conn-with-read-article)
          db (d/db our-conn)]
      (count-read-articles db))
    =>
    1))

(deftest count-all-articles-q-fn-test
  (fact "returns an empty set when there are none"
    (let [our-conn (prepare-migrated-db-conn)
          db (d/db our-conn)]
      (count-all-articles-q db))
    =>
    empty?)

  (fact "returns the count when one read article exists"
    (let [our-conn (prepare-conn-with-existing-article)
          db (d/db our-conn)]
      (ffirst (count-all-articles-q db)))
    =>
    1))

(deftest count-all-articles-fn-test
  (fact "returns 0 when there are none"
    (let [our-conn (prepare-migrated-db-conn)
          db (d/db our-conn)]
      (count-all-articles db))
    =>
    0)

  (fact "returns the count when one read article exists"
    (let [our-conn (prepare-conn-with-existing-article)
          db (d/db our-conn)]
      (count-all-articles db))
    =>
    1))

(deftest count-articles-with-ingest-state-q-fn-test
  (fact "takes an ingest state and returns the count of how many there are"
    (let [our-conn (prepare-conn-with-existing-article)
          db (d/db our-conn)]
      (->> :article.ingest_state/ingested
           (count-articles-with-ingest-state-q db)
           ffirst))
    =>
    1)

  (fact "finds empty set for an ingest state with no articles"
    (let [our-conn (prepare-conn-with-existing-article)
          db (d/db our-conn)]
      (count-articles-with-ingest-state-q db :article.ingest_state/fetched))
    =>
    empty?))

(deftest count-all-articles-with-ingest-state-fn-test
  (fact "returns 0 when there are no articles of given state"
    (let [our-conn (prepare-conn-with-existing-article)
          db (d/db our-conn)]
      (count-articles-with-ingest-state db :article.ingest_state/errored))
    =>
    0)

  (fact "returns the count when articles exist with ingest state"
    (let [our-conn (prepare-conn-with-existing-article)
          db (d/db our-conn)]
      (count-articles-with-ingest-state db :article.ingest_state/ingested))
    =>
    1))

(deftest translate-create-key-names-fn-test
  (facts "it translates a key it knows about"
    (translate-create-key-names {:original_url original-url})
    =>
    {:article/original_url original-url})

  (facts "it filters keys that are not in its list (sanitizes params)"
    (let [params {:something-else "foo"
                  :url original-url}]
      (translate-create-key-names params))
    =>
    {:article/original_url original-url}))

(deftest create-article-fn-test
  (facts "when no article exists"
    (fact "can create an article successfully with original_url"
      (let [our-conn (prepare-migrated-db-conn)]
        (-> (create-article our-conn {:original_url original-url})
            (:article/original_url)))
      =>
      original-url))

  (facts "when an article already exists with the same original-url"
    (fact "it returns the existing entity rather than creating one"
      (let [our-conn (prepare-conn-with-existing-article)]
        ;; we know that our transaction for existing article
        ;; gives us a guid of existing-guid,
        ;; so we check for that here:
        (-> (create-article our-conn {:original_url original-url})
            (:article/guid)))
      =>
      existing-guid)))

(deftest update-article-fn-test
  (facts "when article does not exist"
    (fact "it returns falsey but does not raise error"
      (let [our-conn (prepare-migrated-db-conn)
            article nil
            params {:read true}]
        (update-article our-conn article params))
      =>
      falsey))

  (facts "when the article specified by guid exists"
    (fact "updates the article's attribute"
      (let [our-conn (prepare-conn-with-existing-article)
            article (find-article-by-guid (d/db our-conn) existing-guid)
            params {:read true}]
        (->> params
             (update-article our-conn article)
             (:article/read)))
      =>
      true)))

(deftest translate-update-key-names-fn-test
  (facts "it translates a key it knows about"
    (translate-update-key-names {:read true})
    =>
    {:article/read true})

  (facts "it filters keys that are not in its list (sanitizes params)"
    (let [params {:guid "asdf" :read true}]
      (translate-update-key-names params))
    =>
    {:article/read true}))

(deftest mark-article-as-read-fn-test
  (facts "when the article specified by guid does not exist"
    (fact "returns falsey"
      (let [our-conn (prepare-migrated-db-conn)]
        (mark-article-as-read our-conn "nonexistant"))
      =>
      falsey))

  (facts "when the article specified by guid exists"
    (fact "marks the article as read"
      (let [our-conn (prepare-conn-with-existing-article)]
        (->> existing-guid
             (mark-article-as-read our-conn)
             :article/read))
      =>
      true)))

(deftest translate-update-keys-and-values-fn-test
  (fact "it tries to resolve the keys first"
    (translate-update-keys-and-values {:read "true"})
    =>
    {:article/read true})

  (facts "it turns a read parameter into a Clojure boolean"
    (fact "when it is a string"
      (translate-update-keys-and-values {:read "true"})
      =>
      {:article/read true})

    (fact "when it is already a boolean"
      (translate-update-keys-and-values {:read true})
      =>
      {:article/read true})))

(deftest translate-create-keys-and-values-fn-test
  (fact "it tries to resolve the keys first"
    (translate-create-keys-and-values {:url "http://example.com"})
    =>
    {:article/original_url "http://example.com"}))

(deftest translate-create-values-fn-test
  (facts "original_url param"
    (fact "gets normalized"
      (translate-create-keys-and-values {:url "http://:@example.com/"})
      =>
      {:article/original_url "http://example.com/"})))
