(ns fugue.audio.osc
  (:require [fugue.engine.nodes :as e]))

(defn sin-osc [freq]
  (e/osc "sine" freq))

(defn square [freq]
  (e/osc "square" freq))

(defn saw [freq]
  (e/osc "sawtooth" freq))
