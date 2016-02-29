(ns fugue.engine.envelope
  (:require [fugue.engine.ctx :as ctx]))

(defn perc
  [in attack release]
  (let [gain-node (ctx/create-gain)
        now (ctx/current-time)
        attack-t (+ now attack)
        decay-t (+ attack-t release)]
    (set! (.. gain-node -gain -value) 0)
    (.exponentialRampToValueAtTime (.-gain gain-node) 0.9 attack-t)
    (.exponentialRampToValueAtTime (.-gain gain-node) 0.001 decay-t)
    (.connect in gain-node)
    gain-node))
