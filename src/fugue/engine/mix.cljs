(ns fugue.engine.mix
  (:require [fugue.engine.ctx :as ctx]
            [fugue.engine.nodes :as nodes]))

(defn merge
  "Joins all of the input signals"
  [& ins]
  (let [gain-node (ctx/create-gain)]
    (doseq [in ins]
      (.connect in gain-node))
    gain-node))

(defn feedback
  "Feeds the second input through the first"
  [thru fb]
  (.connect thru fb)
  thru)

(comment

  (feedback (sin-osc 440) (gain 0.4))


  )
