(ns fugue.core
  (:require [fugue.audio.osc :refer [sin-osc saw]]
            [fugue.audio.mix :refer [mult]]
            [fugue.audio.io :refer [out]]))

(enable-console-print!)
(println "Fugue loaded")
(defn beep [freq] (out (mult (saw freq) 0.2)))


(comment
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

)
;;(defn wobble3 [freq]
;;  (let [pitch-spread [(+ freq 0.1) (- freq 0.1)]]
;;    (map pitch-spread wobble2)))

;;(defn wobble-triggered [freq gate]
;;  (gain (wobble2 freq) (env-gen (asdr 0.01 0.3 0.8 1.3) gate)))

;; (atom wobble-gate 1)
;; (wobble-triggered 200 wobble-gate)
;; (in 3 :seconds (swap! wobble-gate 0))


