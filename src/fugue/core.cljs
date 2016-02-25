(ns fugue.core
  (:require [fugue.audio.osc :refer [sin-osc saw]]
            [fugue.audio.mix :refer [mult]]
            [fugue.audio.filter :refer [lpf]]
            [fugue.audio.io :refer [out]]))

(enable-console-print!)
(println "Fugue loaded")

(defn beep [freq] (out (mult (saw freq) 0.2)))

(defn wobble [freq]
  (let [lfo (mult (sin-osc 2) 300)]
    (out (mult (lpf (saw freq) lfo) 0.2))))

(defn wobble2 [freq]
  (let [lfo (mult (sin-osc 2) 300)]
    (-> freq
        saw
        (lpf lfo)
        (mult 0.2)
        out)))

(wobble2 200)
