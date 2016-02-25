(ns fugue.core
  (:require [fugue.audio.osc :refer [sin-osc saw]]
            [fugue.audio.mix :refer [mult]]
            [fugue.audio.io :refer [out]]))

(enable-console-print!)
(println "Fugue loaded")

(defn beep [freq] (out (mult (saw freq) 0.2)))

(beep 240)
(beep 241)
