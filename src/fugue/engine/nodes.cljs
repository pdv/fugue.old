(ns fugue.engine.nodes
  (:require [fugue.engine.ctx :as ctx]))

(defn- set-param!
  "Sets param to constant or attaches modifier"
  [node-param value]
  (if (= (type value) js/Number)
    (set! (.-value node-param) value)
    (.connect value node-param)))

(defn- connect [a b]
  (.connect a b))

(defn osc
  "Creates and starts an OscillatorNode with optional delay"
  [type freq]
   (let [osc-node (ctx/create-oscillator)]
     (set! (.-type osc-node) type)
     (set-param! (.-frequency osc-node) freq)
     (.start osc-node)
     osc-node))

(defn gain
  "Creates a GainNode and attaches it to the input node"
  [in amount]
  (let [gain-node (ctx/create-gain)]
    (set-param! (.-gain gain-node) amount)
    (connect in gain-node)
    gain-node))

(defn biquad-filter
  "Apply a biquad filter to the input signal"
  [in type freq]
  (let [filter-node (ctx/create-biquad-filter)]
    (set! (.-type filter-node) type)
    (set-param! (.-frequency filter-node) freq)
    (connect in filter-node)
    filter-node))

(defn sig-delay
  "Delays in the input signal by the given amount in seconds"
  [in delay-time]
  (let [delay-node (ctx/create-delay (* 2 delay-time))]
    (set-param! (.-delayTime delay-node) delay-time)
    (connect in delay-node)
    delay-node))
