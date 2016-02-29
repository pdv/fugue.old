(ns fugue.demo
  (:require [fugue.audio.osc :refer [sin-osc saw]]
            [fugue.audio.mix :refer [boost gain]]
            [fugue.audio.filter :refer [lpf]]
            [fugue.audio.io :refer [out]]
            [fugue.engine.ctx :refer [reload!]]))

(defn foo [] (out (sin-osc 440)))

(foo)
(reload!)

(defn wobble [freq]
  (let [lfo (gain (sin-osc 2) 300)]
    (out (gain (lpf (saw freq) lfo) 0.2))))

(wobble 440)
(stop)

(defn lfo [freq scale bias]
  (boost (gain (sin-osc freq) scale) bias))

(defn wobble2 [freq cutoff]
  (-> freq
      saw
      (lpf (lfo 2 300 cutoff))
      (gain 0.2)
      out))

(wobble2 200 400)

(wobble2 200 (lfo 0.2 500 400))

(stop)


;;; Music theory

(def C4 60)

(def NOTES
  {:c 0
   :c# 1 :db 1
   :d 2
   :d# 3 :eb 3
   :e 4
   :f 5
   :f# 6 :gb 6
   :g 7
   :g# 8 :ab 8
   :a 9
   :a# 10 :bb 10
   :b 11})

(defn note [letter octave]
  (+ C4 (NOTES letter) (* 12 (- octave 4))))


(defn ding [freq]
  (-> freq
      sin-osc
      (gain 0.2)
      (perc 0.05 0.5)
      out))

(ding 440)

(defn midi->hz [note]
  (* 440.0 (.pow js/Math 2.0 (/ (- note 69.0) 12.0))))

(midi->hz (note :a 3))

(defn ding-note [note]
  (ding (midi->hz note)))

(ding-note (note :a 4))


(def CHORDS
  {:major #{0 4 7}
   :minor #{0 3 7}})

(defn chord [root type]
  (into #{} (map #(+ % root) (CHORDS type))))

(chord (note :c 4) :major)

(defn ding-chord [chord]
  (doseq [note chord] (ding-note note)))

(ding-chord (chord (note :c 4) :major))

(ding-chord (chord (note :d 5) :minor))


(def SCALES
  {:major [2 2 1 2 2 2]
   :minor [2 1 2 2 1 2]
   :blues [3 2 1 1 3]})

(defn scale [root type]
  (reduce
   (fn [notes interval]
     (conj notes (+ (last notes) interval)))
   [root]
   (SCALES type)))

(scale (note :c 4) :major)

