(ns remembrance.core
  (:use compojure.core
        remembrance.api
        remembrance.views
        remembrance.config
        [hiccup.middleware :only (wrap-base-url)]
        ring.middleware.json
        ring.util.response)
  (:require [compojure.route :as route]
            [compojure.handler :as handler]
            [compojure.response :as response]))

(def config (remembrance.config/load!))

;; defroutes macro defines a function that chains individual route
;; functions together. The request map is passed to each function in
;; turn, until a non-nil response is returned.
(defroutes app-routes
  (GET "/" [] (index-page))


  ;; API resources
  (GET "/documents" [] {:body (get-all-documents)})

  ; to serve static pages saved in resources/public directory
  (route/resources "/")

  ; if page is not found
  (route/not-found "Page not found"))

;; site function creates a handler suitable for a standard website,
;; adding a bunch of standard ring middleware to app-route:
(def handler
  (->
   (handler/site app-routes)
   (ring.middleware.json/wrap-json-body)
   (ring.middleware.json/wrap-json-params)
   (ring.middleware.json/wrap-json-response)))
