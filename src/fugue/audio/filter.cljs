(ns fugue.audio.filter
  (:require [fugue.engine :refer [bqfilter]]))

(defn lpf [in freq]
  (bqfilter in "lowpass" freq))
