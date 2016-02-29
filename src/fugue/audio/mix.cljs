(ns fugue.audio.mix
  (:require [fugue.engine.nodes :as e]))

(defn boost [in amp]
  (println "oops"))

(defn gain [in amp]
  (e/gain in amp))

