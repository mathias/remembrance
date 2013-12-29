(ns remembrance.config
  (require [clojure.edn :as edn]))

(def config-file-path "resources/config.edn")

(defn load! []
  (edn/read-string
   (slurp config-file-path)))
