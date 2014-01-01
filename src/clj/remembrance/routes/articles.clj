(ns remembrance.routes.articles
  (:require [playnice.core :refer [dassoc] :as playnice]
            [remembrance.config :as config]
            [remembrance.models.article :as article]
            [remembrance.models.note :as note]
            [remembrance.routes.core :refer [respond-with respond-with-error respond-with-json]]
            [remembrance.workers :refer [enqueue-article-original-html]]
            [ring.util.response :refer [redirect]]
            [taoensso.timbre :refer [info]]))

(def env (config/load!))

(defn article-index-url []
  ;; TODO: Use cemerick.url to compose URLs
  (str (env :hostname) "/api/articles"))

(defn article-show-url [guid]
  ;; TODO: Use cemerick.url to compose URLs
  (str (env :hostname) "/api/articles/" guid))

(defn article-wrap-json [article]
  {:href (article-show-url (:article/guid article))
   :guid (:article/guid article)
   :title (:article/title article)
   :original_url (:article/original_url article)
   :read (:article/read article)
   })

(defn full-article-wrap-json [full-article]
  {:href (article-show-url (:article/guid full-article))
   :guid (:article/guid full-article)
   :title (:article/title full-article)
   :original_url (:article/original_url full-article)
   :readable_body (:article/readable_body full-article)
   :read (:article/read full-article)
   })

(defn article-collection-json [collection]
  (map article-wrap-json collection))

(defn create-and-enqueue-article [params]
  (let [article (article/create-article params)
        guid (:article/guid article)]
    (enqueue-article-original-html guid)
    (redirect (article-show-url guid))))


(defn article-routes []
  ;;  (GET "/" [] (respond-with-json {:articles (article-collection-json (article/find-all-ingested-articles))}))

)
;; (create-and-enqueue-article params)
  ;; (GET "/search" {:keys [params]} (respond-with-json (article-collection-json (article/search-articles (:q params)))))
  ;; (GET "/stats" [] (respond-with-json (article/articles-stats)))
  ;; (GET "/:guid" [guid] (let [article (article/show-article guid)]
  ;;                        (if-not (nil? article)
  ;;                          (respond-with-json (full-article-wrap-json article))
  ;;                          (respond-with-error)))))

(defn index-path [req]
  (respond-with "hi haters"))

(defn show-article [req] {})
(defn search [req] {})
(defn stats [req] {})
