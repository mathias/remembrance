(ns remembrance.routes.response-schemas
  (:require [schema.core :as s]))

(def Uri
  "A URI represented by a string"
  s/Str)

(def Guid
  "A GUID for a resource"
  s/Str)

(def FullArticle
  "JSON schema for a full article's content"
  {:href Uri
     :guid Guid
     :title s/Str
     :original_url Uri
     :readable_body s/Str
     :read s/Bool})

(def ArticleInfo
  "JSON schema for individual article"
  {:href Uri
   :guid Guid
   :title s/Str
   :original_url Uri
   :read s/Bool})

(def FullArticleList
  "JSON schema for individual full article, but we always put it in an articles array"
  {:articles [FullArticle]})

(def ArticleList
  "JSON schema for article list"
  {:articles [ArticleInfo]})

(def ArticleStats
  "Stats about articles"
  {:total s/Int
   :ingested s/Int
   :fetched s/Int
   :errored s/Int
   :read s/Int})

(def NoteInfo
  "JSON schema for an individual note"
  {:guid Guid
   :title s/Str
   :body s/Str
   :href Uri
   :articles [Guid]})

(def NoteList
  "JSON schema for notes list"
  {:notes [NoteInfo]})

(def NoteStats
  "Stats about articles"
   {:total s/Int})

(def ApiStats
  "Stats for the entire system"
  {:stats
   {:articles ArticleStats
    :notes NoteStats}})

(def Timestamp
  "A timestamp -- String for now until it can be further validated"
  s/Str)

(def ApiHealth
  "Health checks for system in dev"
  {:health
   {:timestamp Timestamp
    :database-value s/Str
    :pid s/Int
    :process-uptime s/Int
    :memory {:free s/Int
             :total s/Int}
    :os {:hostname s/Str}}})
