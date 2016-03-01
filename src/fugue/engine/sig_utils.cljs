(ns fugue.engine.sig-utils
  (:require [fugue.engine.ctx :as ctx]
            [fugue.engine.nodes :as nodes]))

(defn noise [amount]
  (let [buffer-size (* 2 (ctx/sample-rate))
        buffer      (ctx/buffer buffer-size #(- (.random js/Math) 1))
        source      (ctx/buffer-source buffer true)
        out         (nodes/gain source amount)]
    (.start source)
    out))

(defn dc [amount]
  (let [buffer-size (* 2 (ctx/sample-rate))
        buffer      (ctx/buffer buffer-size #(identity 0.95))
        source      (ctx/buffer-source buffer true)
        out         (nodes/gain source amount)]
    (.log js/console (.getChannelData buffer 0))
    (.start source)
    out))
