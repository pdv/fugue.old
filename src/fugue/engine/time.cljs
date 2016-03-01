(ns fugue.engine.time
  (:require [fugue.engine.ctx :as ctx]))

(defn now []
  (ctx/current-time))

(defn at
  "Schedules function to be executed at time (seconds)"
  [time fun]
  (js/setTimeout fun (* 1000 (- time (now))))
  time)

