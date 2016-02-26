(ns fugue.audio.filter
  (:require [fugue.engine :as e]))

(defn lpf [in freq]
  (e/biquad-filter in "lowpass" freq))
