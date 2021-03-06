(ns fugue.demo
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :as async]
            [fugue.audio :as a]
            [fugue.midi :as m]))


;; Actual demo

;; Capture MIDI 

(def oxy (midi/midi-mono "Oxygen 49"))
(def oxy-ctrl (partial midi/midi-ctrl "Oxygen 49"))
(def osc1-vol (oxy-ctrl 73 0 1))
(def osc2-vol (oxy-ctrl 72 0 1))
(def osc3-vol (oxy-ctrl 15 0 1))
(def osc4-vol (oxy-ctrl 78 0 1))
(def filt-freq (oxy-ctrl 9 10 5000))
(def filt-q (oxy-ctrl 75 0.01 10))
(def lfo-rate (oxy-ctrl 77 0.01 20))
(def lfo-amt (oxy-ctrl 7 0 2000))
(def detune (oxy-ctrl 1 0 25))
(def detune- (oxy-ctrl 1 0 -25))

;; Let's define a synth

(defn synth [in]
  (let [freq (:freq in)
        gate (:gate in)
        osc1 (gain (saw freq detune) osc1-vol)
        osc2 (gain (saw freq detune-) osc2-vol)
        osc3 (gain (sin-osc freq -1200) osc3-vol)
        osc4 (gain (square freq) osc4-vol)
        lfo (gain (sin-osc lfo-rate) lfo-amt)
        env (audio/adsr 0.05 0.05 0.9 3.5)]
    (-> (audio/mix osc1 osc2 osc3 osc4)
        (lpf (audio/cv filt-freq lfo) filt-q)
        (gain (audio/env-gen env gate))
        out)))

(synth oxy)

;;;;;;;;

(defn out [in]
  (a/out (a/gain in 0.5)))


(a/init-audio!)

(m/midi-init!)

(m/midi-in-devices)

(def lp (m/midi-in "Launchpad"))

(def ctrl (m/midi->cv lp))


(out (a/gain (a/saw (:freq ctrl)) (a/env-gen (a/adsr 0.05 0.1 0.5 0.2) (:velocity ctrl))))


(async/put! gate 1)

(async/put! gate 0)

(defprotocol Foo
  (bar [x]))


(extend-protocol Foo
  number
  (bar [x]
    "Doubles x."
    (* 2 x)))

(doc bar)

(def foo {:penis 5})

(:bar foo)
(if (:bar foo) 3)

(out (fb (sin-osc 440) #(sig-delay % 0.4)))

(def c (async/chan))

(type (async/chan))

(out (gain (sin-osc 440) (env-gen (perc 0.5 0.5) c)))

(async/put! c 1)  
(perc 0.5 0.5)
(map (partial * 1) (:on-levels (perc 0.5 0.5)))

(ns fugue.demo)

(defn foo
  ([a b] (foo a b 1))
  ([a b c] (+ a b c)))

(def bar (partial foo 3))

(doc bar)


;;;
;;; Variations on a theme
;;; pdv
;;;

(defn demo [in]
  (out (gain in 0.5)))

;;;;;;;;;;;;;;;;;;;;;; 1

(demo (sin-osc 440))
;;=> 1

(kill)

;;;;;;;;;;;;;;;;;;;;;; 2

(out (sin-osc 440))
;;=> 1
(kill)

(out (sin-osc 880))
;;=> 1
(kill)

(out (sin-osc 220))
;;=> 1
(kill)

;;;;;;;;;;;;;;;;;;;;;; 3

(out (mix (sin-osc 440) (sin-osc 441)))
;;=> 1
(kill)

(out (sin-osc [440 441]))
;;=> 1
(kill)

;; The above translates to
;; (out [(sin 440) (sin 441)])
;; (apply f args) unfolds args

(out (apply mix (sin-osc [440 441])))

;;;;;;;;;;;;;;;;;;;;;; 4

(out (gain (sin-osc 440) (env-gen (perc 0.3 0.5))))

(defn ding! [freq]
  (gain (sin-osc freq) (env-gen (perc 0.3 0.5))))

(out (ding! 440))

;;;;;;;;;;;;;;;;;;;;;; 5

(out (saw 440))

(out (lpf (saw 440) 600))

(-> (saw 440)
    (lpf 600)
    out)

;;;;;;;;;;;;;;;;;;;;;; 6

(defn pluck! [freq]
  (let [env (env-gen (perc 0.05 0.3))
        osc (saw freq)])
  (-> sin-osc
      (lpf env)
      (gain env)
      out))

(pluck! 640)

;;;;;;;;;;;;;;;;;;;;;; 7
;; MONOPHONY

(defn minimoog [midi-note]
  (let [freq (:midi))
        velocity (:velocity midi)
        env (env-gen (adsr 0.05 0.3 0.8 0.1) velocity)]
    (-> (saw freq)
        (lpf env)
        (gain env)
        out)))

(minimoog {:note 60 :velocity 0.8})
(kill)

(minimoog (midi-in "Launchpad" :note))


;;;;;;;;;;;;;;;;;;;;;; 8
;; POLYPHONY


(defn polymoog [note velogate]
  )





;; Env demo

(defn out [output]
  (io/out (gain output 0.5)))

(def gate (atom 1))

(def note->freq identity)

(defn buzz [note velocity gate]
  (let [velocity (/ velocity 127)
        env (env-gen (adsr 0.03 0.15 0.8 0.3) gate velocity)]
    (-> note
        note->freq
        saw
        lpf 330
        amp env)))

(defn buzz-synth [cutoff volume]
  (mono-synth
   (fn [note velocity gate]
     (let []))))

(defn better-synth [cuffoff volume]
  (fn [midi-in]
    (-> midi-in
        midi->freq   ; (comp midi->note note->freq)
        saw
        (lpf cutoff)
        (amp (env-gen (asr 0.3 0.8 0.3) (:velocity midi-in))))))

(defn minimoog [midi]
  (let [freq (note->freq (:note midi))
        velocity (:velocity midi)
        amp-env (env-gen (adsr 0.02 0.4 0.8 1.3) velocity)]
    (-> (sin-osc freq)
        (lpf 200)
        (amp ))))

(defn best-synth [midi]
  (-> (note->freq midi)
      sin-osc
      (lpf 200)
      amp ()))
      amp
      (env-gen (adsr 0.3 1.2 0.5 ))
      (lpf 200)



(comment
  (fn [midi-in]
    (-> midi-in
        note)
    (let [velocity (:velocity midi-in)
          ])))

(out ((mono-synth buzz) (midi-in "Launchpad")))

(out (buzz 300 85 gate))



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

