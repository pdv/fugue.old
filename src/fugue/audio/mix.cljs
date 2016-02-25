(ns fugue.audio.mix
  (:require [fugue.engine :refer [gain]]))

(defn mult [in amp]
  (gain in amp))
