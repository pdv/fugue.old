(ns fugue.audio.filter
  (:require [fugue.engine :as e]))

(comment
(defn lpf [in freq]
  (bqfilter in "lowpass" freq))
)
