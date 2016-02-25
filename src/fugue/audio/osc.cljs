(ns fugue.audio.osc
  (:require [fugue.engine :refer [osc]]))

(defn sin-osc [freq]
  (osc "sine" freq))

(defn square [freq]
  (osc "square" freq))

(defn saw [freq]
  (osc "sawtooth" freq))
