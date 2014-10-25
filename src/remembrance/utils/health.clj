(ns remembrance.utils.health
  (:require [clojure.string :as string]))

(defn current-date []
  (str (java.util.Date.)))

(defn current-pid []
  (-> (java.lang.management.ManagementFactory/getRuntimeMXBean)
      (.getName)
      (string/split #"@")
      (first)
      (read-string)))

(defn process-uptime []
  (.getUptime (java.lang.management.ManagementFactory/getRuntimeMXBean)))


(defn actual-used-memory []
  ;; per http://stackoverflow.com/a/18375641/1091712
  (let [rt (Runtime/getRuntime)
        allocated-free-memory (.freeMemory rt)
        allocated-total-memory (.totalMemory rt)
        used-memory (- allocated-total-memory allocated-free-memory)
        max-memory (.maxMemory rt)
        real-free-memory (- max-memory used-memory)]
    {:free real-free-memory
     :stack-total allocated-total-memory
     :os-total max-memory}))

(defn hostname []
  (.. java.net.InetAddress getLocalHost getHostName))

(defn health-stats []
  {:timestamp (current-date)
   :database-value (str (remembrance.database/db))
   :pid (current-pid)
   :process-uptime (process-uptime)
   :memory (actual-used-memory)
   :os {:hostname (hostname)
        ;;:uptime 0
        }})
