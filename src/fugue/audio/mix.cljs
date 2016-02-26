(ns fugue.audio.mix
  (:require [fugue.engine :as e]))

(defn boost [in amp]
  (e/boost in amp))

(defn gain [in amp]
  (e/gain in amp))

