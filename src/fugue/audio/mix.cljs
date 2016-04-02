(ns fugue.audio.mix
  (:require [fugue.engine :as engine]))

(defn gain [in amp]
  (engine/gain in amp))

