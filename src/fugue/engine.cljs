(ns fugue.engine)

(defonce ctx (js/AudioContext.))

(defn osc
  "Create and start an oscillator"
  [type freq]
  (let [osc-node (.createOscillator ctx)]
    (set! (.-type osc-node) type)
    (set! (.. osc-node -frequency -value) freq)
    (.start osc-node)
    (.log js/console osc-node)
    osc-node))

(defn gain
  "Apply a gain to the input signal"
  [in amount]
  (let [gain-node (.createGain ctx)]
    (set! (.. gain-node -gain -value) amount)
    (.connect in gain-node)
    gain-node))

(defn out
  "Connect the signal to browser out"
  [in]
  (.log js/console in)
  (.connect in (.-destination ctx)))
