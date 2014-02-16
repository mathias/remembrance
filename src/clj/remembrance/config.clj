(ns remembrance.config
  (:require [clojure.edn :as edn]))

(defonce config (atom {}))

(defonce config-file-path "resources/config.edn")

(defn load! []
  (if-not (empty? @config)
    @config
    (swap! config
           (fn [_ config] config)
           (edn/read-string
            (slurp config-file-path)))))
