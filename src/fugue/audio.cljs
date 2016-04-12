(ns fugue.audio
  (:require [fugue.engine :as engine]))

(defonce ctx (atom nil))
(defonce buffer-pool (atom {}))

(defn init-audio! []
  (reset! ctx (engine/make-ctx)))

(defn reset-audio! []
  (engine/close! @ctx)
  (init-audio!))

(defn out [in]
  (engine/out @ctx in))

;;; ugens

(defn gain
  "Multiplies the amplitude of in by amp"
  [in amp]
  (engine/gain @ctx in amp))


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

(comment
  ;; FX
  ring-mod
  ping-pong? (can you do this with other audio?)
  flanger amount rate

  )
