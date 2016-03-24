(ns fugue.audio.filter
  (:require [fugue.engine.nodes :as nodes]))

(defn lpf [in freq]
  (nodes/biquad-filter in "lowpass" freq))
