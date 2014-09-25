#!/usr/bin/env boot

#tailrecursion.boot.core/version "2.5.1"

(set-env!
  :project 'remembrance
  :version "0.2.0-SNAPSHOT"
  :main-class 'remembrance.core
  :dependencies (read-string (slurp "deps.edn"))
  :out-path     "resources/public"
  :src-paths    #{"src/hoplon" "src/clj"})

;; Static assets
(add-sync! (get-env :out-path) #{"src/static"})

(require
 '[tailrecursion.hoplon.boot :refer :all]
 '[tailrecursion.boot.task.ring :as r]
 '[remembrance.core :refer [remembrance-handler]]
 '[environ.core :refer [env]])

(deftask heroku
  "Prepare project.clj and Procfile for Heroku deployment."
  [& [main-class]]
  (let [jar-name   (format "%s-standalone.jar" (get-env :project))
        jar-path   (format "target/%s" jar-name)
        main-class (or main-class (get-env :main-class))]
    (set-env!
      :src-paths #{"resources"}
      :lein      {:min-lein-version "2.0.0"
                  :uberjar-name     jar-name
                  :plugins          '[[lein-environ "1.0.0"]]
                  :profiles         {:production {:env {:production true}}}})
    (comp
      (lein-generate)
      (with-pre-wrap
        (-> "project.clj" slurp
          (.replaceAll "(:min-lein-version)\\s+(\"[0-9.]+\")" "$1 $2")
          ((partial spit "project.clj")))
        (-> "web: java $JVM_OPTS -cp %s clojure.main -m %s $PORT"
          (format jar-path main-class)
          ((partial spit "Procfile")))))))

(deftask my-middleware
  []
  (r/ring-task (fn [handler]
                 (fn [req]
                   (remembrance-handler req)))))

(deftask my-ring-server
  []
  (comp (r/reload)
        (r/head)
        (r/dev-mode)
        (r/session-cookie)
        (r/files)
        (my-middleware)
        (r/reload)
        (r/jetty :port (env :port))))

(deftask development
  "Start local dev server."
  []
  (comp
    (watch)
    (my-ring-server)
    (hoplon {:prerender false
             :pretty-print true
             :source-map true})))

(deftask production
  "Compile application with Google Closure advanced optimizations."
  []
  (hoplon {:optimizations :whitespace}))
