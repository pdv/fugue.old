(ns fugue.audio.io
  (:require [fugue.engine :as engine]))

(defn out [in]
  (engine/out in))

(defn mi(ns fugue.audio.io)
  "TODO"
  [& chains]
  (apply gain chains))

(defn parallel
  "TODO"
  [in & chains]
  (merge (apply chains in)))
