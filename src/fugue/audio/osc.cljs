(ns fugue.audio.osc
  (:require [fugue.engine :as e]))

(defn sin-osc [freq]
  (e/osc "sine" freq))

(defn square [freq]
  (e/osc "square" freq))

(defn saw [freq]
  (e/osc "sawtooth" freq))
