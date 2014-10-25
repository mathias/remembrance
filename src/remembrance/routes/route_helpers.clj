(ns remembrance.routes.route-helpers
  (:require [cemerick.url :refer [url url-encode]]
            [environ.core :refer [env]]))

(defn api-route-for [path]
  (str (assoc (url (env :hostname)) :path path)))

(def articles-index-url (api-route-for "/api/articles"))
(def article-show-uri-template (api-route-for "/api/articles/{guid}"))
(def articles-search-uri-template (api-route-for "/api/articles/search?q={query}"))
(def articles-stats-url (api-route-for "/api/articles/stats"))

(def notes-index-url (api-route-for "/api/notes"))
(def note-show-uri-template (api-route-for "/api/notes/{guid}"))
(def notes-search-uri-template (api-route-for "/api/notes/search?q={query}"))

(def api-stats-url (api-route-for "/api/stats"))
(def api-health-url (api-route-for "/api/health"))

(defn article-show-url [guid]
  (api-route-for (str "/api/articles/" guid)))

(defn note-show-url [guid]
  (api-route-for (str "/api/notes/" guid)))


