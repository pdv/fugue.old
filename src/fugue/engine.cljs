(ns fugue.engine)

(defonce ^:dynamic ctx (atom (js/AudioContext.)))

(defn stop
  "Destroy the current context and build a new one"
  []
  (.close @ctx)
  (reset! ctx (js/AudioContext.)))

(defn- param [node-param value]
  (if (= (type value) js/Number)
    (set! (.-value node-param) value)
    (.connect value node-param)))

(defn osc
  "Creates and starts an OscillatorNode"
  [type freq]
  (let [osc-node (.createOscillator @ctx)]
    (set! (.-type osc-node) type)
    (set! (.. osc-node -frequency -value) freq)
    (.start osc-node)
    osc-node))

(defn perc
  [in attack release]
  (let [gain-node (.createGain @ctx)
        now (.-currentTime @ctx)
        attack-t (+ now attack)
        decay-t (+ attack-t release)]
    (set! (.. gain-node -gain -value) 0)
    (.exponentialRampToValueAtTime (.-gain gain-node) 0.9 attack-t)
    (.exponentialRampToValueAtTime (.-gain gain-node) 0.001 decay-t)
    (.connect in gain-node)
    gain-node))

(defn boost
  "Adds the given amount to the input"
  [in amount]
  (let [gain-node (.createGain @ctx)]
    (param (.-gain gain-node) amount)
    (.connect in (.-gain gain-node))
    gain-node))

(defn gain
  "Creates a GainNode and attaches it to the input"
  [in amount]
  (let [gain-node (.createGain @ctx)]
    (param (.-gain gain-node) amount)
    (.connect in gain-node)
    gain-node))

(defn biquad-filter
  "Apply a biquad filter to the input signal"
  [in type freq]
  (let [filter-node (.createBiquadFilter @ctx)]
    (set! (.-type filter-node) type)
    (param (.-frequency filter-node) freq)
    (.connect in filter-node)
    filter-node))

(defn out
  "Connect the signal to browser out"
  [in]
  (.connect in (.-destination @ctx)))
