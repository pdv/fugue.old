(ns fugue.core
  (:require [engine]))

(enable-console-print!)
(println "Hello world!")

(def audio (engine/audioEngine))

(defn sin-osc [freq]
  (.sinosc audio freq))

(defn gain [input gain]
  (.gain audio input gain))

(defn out [input]
  (.out audio input))

(defn beep [] (out (gain (sin-osc 240) 0.2)))

(beep)
