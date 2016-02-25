(ns fugue.audio.io
  (:require [fugue.engine :as e]))

(defn out [in]
  (e/out in))
