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
  (engine/out @ctx in)
  in)

(defn cv [value node]
  {:value value :node node})

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
  (fn [gate now]
    (if (> gate 0)
      [{:value gate
        :time (+ now a)}
       {:value (* s gate)
        :time (+ now a d)}]
      [{:value 0
        :time (+ now r)}])))

(defn perc [a r]
  (fn [gate now]
    (if (> gate 0)
      [{:value gate
        :time (+ now a)}
       {:value 0
        :time (+ now a r)}])))

(defn env-gen [env gate]
  (engine/EnvGen. env gate))

;; (defn env-gen [env gate]
;;   (let [ch (async/chan)]
;;     (go
;;       (while true
;;         (let [g (<! gate)
;;               msgs (if (> g 0) (:on env) (:off env))]
;;           (js/console.log "gate message received")
;;           (>! ch {})
;;           (doseq [msg msgs]
;;             (>! ch msg)))))
;;     ch))
