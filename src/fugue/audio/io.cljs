(ns fugue.audio.io
  (:require [fugue.engine.ctx :as ctx]))

(defn out [in]
  (ctx/out in))
