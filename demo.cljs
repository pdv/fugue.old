(ns fugue.demo
  (:require [fugue.audio.osc :refer [sin-osc saw]]
            [fugue.audio.mix :refer [boost gain]]
            [fugue.audio.filter :refer [lpf]]
            [fugue.audio.io :as io]
            [fugue.transport :refer [stop]]
            [fugue.engine.sig-utils :refer [noise dc]]
            [fugue.engine.time :refer [now at]]
            [fugue.engine.envelope :refer [perc env-test]]
            [fugue.engine.ctx :as ctx]))

(defn out [output]
  (io/out (gain output 0.5)))

(out (noise 0.2))
(stop)
(out (sin-osc 440))
(stop)

(defn foo [freq]
  (out (sin-osc freq)))

(foo 440)
(foo 441)
(stop)

(defn wobble [freq]
  (let [lfo (gain (sin-osc 2) 300)]
    (out (lpf (saw freq) lfo))))

(wobble 220)
(stop)

(defn lfo [freq scale]
  (gain (sin-osc freq) scale))

(defn wobble2 [freq]
  (-> freq
      saw
      (lpf (lfo 2 300))
      out))


(wobble2 220)
(wobble2 (lfo 0.2 50))
(stop)




;; Midi Notes

(defn ding! [freq]
  (-> freq
      sin-osc
      (perc 0.2 1.3)
      out))

(ding! 440)


(defn midi->hz [note]
  (* 440.0 (.pow js/Math 2.0 (/ (- note 69.0) 12.0))))

(defn ding-note! [note]
  (ding! (midi->hz note)))

(ding-note! 60)

(defn ding-chord! [notes]
  (doseq [note notes]
    (ding-note! note)))

(ding-chord! [60 64 67])



;; Timing

(now)
(at (+ 6 (now)) #(ding-note! 60))

(defn note [time pitch] {:time time :pitch pitch})

(note 3 60)

(defn where [k f notes] (->> notes (map #(update-in % [k] f))))
(defn from [offset] (partial + offset))

(defn play! [notes]
  (let [scheduled-notes (->> notes (where :time (from (now))))]
    (doseq [{time :time note :pitch} scheduled-notes]
      (at time #(ding-note! note)))
    scheduled-notes))

(defn even-melody! [pitches]
  (let [times (reductions + (repeat 0.2))
        notes (map tnote times pitches)]
    (play! notes)))


(even-melody! (range 60 67))



;; Music Theory


(defn scale [intervals]
  (fn [degree]
    (reductions + degree (cycle intervals))))

(defn scale [intervals]
  (fn [degree]
    (reduce
      (fn [notes interval]
        (conj notes (+ (last notes) interval)))
      [degree]
      (cycle intervals))))


(def major (scale [2 2 1 2 2 1]))
(take 5 (major 60))

(.log js/console major)



(def minor (scale [2 1 2 2 1 2]))

(def C (from 60))
(defs [D E F G A B]
  (map
   (comp from C major)
   (rest (range))))

(def sharp inc)
(def flat dec)

(take 8 (comp C sharp minor))



;;; --------------------------------
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
      (perc 0.2 1.3)
      (gain 0.5)
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
(ding-chord (chord (note :g 4) :major))
(ding-chord (chord (note :a 4) :minor))
(ding-chord (chord (note :f 4) :major))

(stop)


(def SCALES
  {:major [2 2 1 2 2 2 1]
   :minor [2 1 2 2 1 2 2]
   :blues [3 2 1 1 3 2]})

(defn scale [root type]
  (reduce
   (fn [notes interval]
     (conj notes (+ (last notes) interval)))
   [root]
   (SCALES type)))

(scale (note :c 4) :major)

