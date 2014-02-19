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
  (-> (java.lang.management.ManagementFactory/getRuntimeMXBean)
      (.getUptime)))

(defn free-memory []
  (.freeMemory (Runtime/getRuntime)))

(defn total-memory []
  (.totalMemory (Runtime/getRuntime)))

(defn hostname []
  (.. java.net.InetAddress getLocalHost getHostName))

(defn health-stats []
  {:timestamp (current-date)
   :database-value (str (remembrance.database/db))
   :pid (current-pid)
   :process-uptime (process-uptime)
   :memory {:free (free-memory)
            :total (total-memory)}
   :os {:hostname (hostname)
        ;;:uptime 0
        }})
