(ns remembrance.classifier
  (require [fujiin.mjolniir :as mjolniir]))

(defn count-words [word words]
  (count (filter #(= word %) words)))

(defn word-map [k words]
  {k (count-words k words)})

(defn make-dictionary-map [doc]
  (let [words (mjolniir/create-tokens doc)
        our-keys (distinct words)]
    (map (fn [k] (word-map k words)) our-keys)))

(defn total-vocab [docs]
  (vec (sort (distinct (flatten docs)))))
