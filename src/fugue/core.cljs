(ns fugue.core
  (:require [engine]))

(enable-console-print!)
(println "Hello world!")

(def audio (engine/audioEngine))


(.sinosc audio 400)
