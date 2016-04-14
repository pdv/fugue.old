(ns fugue.audio
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :as async]
            [fugue.engine :as engine]))

(defonce ctx (atom nil))
(defonce buffer-pool (atom {}))

(defn init-audio! []
  (reset! ctx (engine/make-ctx)))

(defn reset-audio! []
  (engine/close! @ctx)
  (init-audio!))

(defn out [in]
  (engine/out @ctx in)
  in)


;;; Mix

(defn gain
  "Multiplies the amplitude of in by amp"
  [in amp]
  (engine/gain @ctx in amp))

(defn mix
  "Combines the inputs into one signal"
  [& args]
  (engine/mix @ctx args))


;;; Oscillators

(defn sin-osc
  "Starts a sine wave oscillator at the given frequency"
  [freq]
  (engine/oscillator @ctx :sine freq 0))

(defn saw
  "Starts a saw wave oscillator at the given frequency"
  [freq]
  (engine/oscillator @ctx :sawtooth freq 0))

(defn square
  "Starts a square wave oscillator at the given frequency"
  [freq]
  (engine/oscillator @ctx :square freq 0))

(defn tri
  "Starts a triangle wave oscillator at the given frequency"
  [freq]
  (engine/oscillator @ctx :triangle freq 0))


;;; Filters

(defn lpf
  "Applies a low-pass filter to the input"
  ([in freq] (lpf in freq 1))
  ([in freq q]
   (engine/biquad-filter @ctx in :lowpass freq q)))

(defn hpf
  "Applies a high-pass filter to the input"
  ([in freq] (lpf in freq 1))
  ([in freq q]
   (engine/biquad-filter @ctx in :highpass freq q)))

(defn bpf
  "Applies a band-pass filter to the input"
  ([in freq] (lpf in freq 1))
  ([in freq q]
   (engine/biquad-filter @ctx in :bandpass freq q)))


;;; Env-gen

(defn adsr [a d s r]
  {:on [{:value 1
         :time a}
        {:value s
         :time d}]
   :off [{:value 0
          :time r}]})

(defn perc [a r]
  {:on-levels [1 0]
   :on-times [a r]})

(defn env-gen [env gate]
  (let [ch (async/chan)]
    (go
      (while true
        (let [g (<! gate)
              msgs (if (> g 0) (:on env) (:off env))]
          (js/console.log "gate message received")
          (>! ch {})
          (doseq [msg msgs]
            (>! ch msg)))))
    ch))


(comment
  ;; FX
  ring-mod
  ping-pong? (can you do this with other audio?)
  flanger amount rate

  )

(defn midi->cv [midi]
  {:note (filter :note midi)
   :velocity (filter :velocity midi)}
  (out (sin-osc :note)))

