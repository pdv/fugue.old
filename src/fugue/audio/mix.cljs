(ns fugue.audio.mix
  (:require [fugue.engine :as engine]))

(defn gain [in amp]
  (engine/gain in amp))

(defn pan [in p]
  ; TODO
  (engine/gain in 1))

(defn amp [in g p]
  ; comp?
  (-> in (gain g) (pan p)))
