(ns fugue.audio.mix
  (:require fugue.engine))

(defn mult [in amp]
  (fugue.engine/gain in amp))
