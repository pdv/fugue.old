(ns fugue.audio.filter
  (:require [fugue.engine.nodes :as e]))

(defn lpf [in freq]
  (e/biquad-filter in "lowpass" freq))
