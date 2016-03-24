(ns fugue.engine.time
  (:require [fugue.engine.ctx :as ctx]
            [fugue.engine.nodes :as nodes]))

(defn now []
  (ctx/current-time))

(defn after
  "Inserts a DelayNode with specified delay time"
  [delay-t in]
  (nodes/delay in delay-t))

(defn at
  "Delays the input signal to play at the specified time"
  [time in]
  (nodes/delay in (- time (now))))

(defn every
  [time in]
  (mix/feedback (nodes/gain in 1) (nodes/delay in time)))

(defn- every-old
  "Repeats the input signal after specified delay"
  [time in]
  (let [gain-node (nodes/gain in 1)
        delay-node (nodes/delay in time)]
    (.connect gain-node delay-node)
    (.connect delay-node gain-node)
    gain-node))

