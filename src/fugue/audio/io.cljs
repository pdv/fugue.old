(ns fugue.audio.io
  (:require [fugue.engine.ctx :as ctx]))

(defn out [in]
  (ctx/out in))

(defn mix
  "TODO"
  [& chains]
  (apply gain chains))

(defn parallel
  "TODO"
  [in & chains]
  (merge (apply chains in)))
