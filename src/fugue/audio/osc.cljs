(ns fugue.audio.osc
  (:require [fugue.engine :as engine]))

(defn sin-osc [freq]
  (engine/oscillator :sine freq))

(defn square [freq]
  (engine/oscillator :square freq))

(defn saw [freq]
  (engine/oscillator :sawtooth freq))

