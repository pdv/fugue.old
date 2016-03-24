(ns fugue.audio.osc
  (:require [fugue.engine.nodes :as nodes]))

(defn sin-osc [freq]
  (nodes/osc "sine" freq))

(defn square [freq]
  (nodes/osc "square" freq))

(defn saw [freq]
  (nodes/osc "sawtooth" freq))
