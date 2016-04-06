(ns fugue.audio.filter
  (:require [fugue.engine :as engine]))

(defn lpf [in freq]
  (engine/biquad-filter in :lowpass freq))
