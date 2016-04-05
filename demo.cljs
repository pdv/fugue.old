(ns fugue.demo
  (:require [fugue.audio.osc :refer [sin-osc saw]]
            [fugue.audio.mix :refer [gain]]
            [fugue.audio.filter :refer [lpf]]
            [fugue.audio.io :as io]
            [fugue.transport :refer [stop]]
            [fugue.envelope :refer [adsr perc env-gen]]
            [fugue.midi :refer [midi-init midi-in midi-devices]]))

(defn out [output]
  (io/out (gain output 0.5)))

(def gate (atom 1))

(defn buzz [freq]
  (gain (saw freq) (env-gen (adsr 0.5 0.5 0.2 0.5) gate)))

(out (buzz 220))

(reset! gate 0)

(reset! gate 1)

(stop)


;; Experiments

 (defn epiano [freq velocity gate]
   (-> freq
       (parallel
        (sin-osc)
        (saw))
       (env-gen (asdr 0.01 0.1 0.8 0.3) gate velocity)
       (ping-pong 0.4 0.2)
       (reverb 0.3)
       out))

 ((midi-wrap epiano) (midi-in "oxygen49"))

 ;; End Experiments


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


(def major (scale [2 2 1 2 2 1]))
(def minor (scale [2 1 2 2 1 2]))

(take 8 (major 60))


(def C 60)
(defs [D E F G A B]
  (map
   (comp from C major)
   (rest (range))))

(def sharp inc)
(def flat dec)


(take 8 (-> (C) sharp major))



;;; --------------------------------
;;; Music theory


(defn metronome [bpm]
  (reductions + (now) (repeat (/ 60 bpm))))

(take 5 (metronome 120))

(def metro (metronome 120))

(take 5 metro)


(defn foo1 [a b]
  (+ a b))

(defn foo2 [a]
  (partial foo1 a))


(def foo (partial + 2))

(foo 2)

(defn bar [fun]
  (+ 3 fun))

(bar (foo 0))


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

;;; -------------------------------------
;;; Experiments

(defn play! [freq]
  (out (sin-osc freq)))

(defn sin-osc [freq]
  (partial osc "sine" freq))

(defn at [time fn]
  (call fn (- time (now))))

(def metro (metronome 120 4 4))

; bar 2, beat 2-a
(at (metro 2 2 3) (sin-osc 40))

(at (metro 2 2) (hold (inst 230)))

(defn wobbly [freq rate amount gate]
  (let [lfo (gain (sin-osc rate) amount)]
    (-> freq
        saw
        (lpf (lfo 2 300))
        (gain (env-gen (asdr 0.01 0.1 0.8 0.1) gate 1 0))
        out)))

(def midi-wobbly (midi-player wobbly))

