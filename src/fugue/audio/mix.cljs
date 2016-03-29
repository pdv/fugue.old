(ns fugue.audio.mix
  (:require [fugue.engine.nodes :as nodes]))

(defn gain [in amp]
  (nodes/gain in amp))

