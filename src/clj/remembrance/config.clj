(ns remembrance.config
  (:require clojure.edn))

(def config-file-path "resources/config.edn")

(defn load! []
  (clojure.edn/read-string
   (slurp config-file-path)))
