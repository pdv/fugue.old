(ns fugue.engine
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :as async :refer [<!]]))


;; AudioContext

(defn make-ctx [] (js/AudioContext.))

(defn now [ctx]
  (.-currentTime ctx))

(defn sample-rate [ctx]
  (.-sampleRate ctx))

(defn resume! [ctx]
  (.resume ctx))

(defn suspend! [ctx]
  (.suspend ctx))

(defn close! [ctx]
  (.close ctx))

(defn out [ctx in]
  (.connect in (.-destination ctx))
  in)

(def audio? (partial instance? js/AudioNode))



;; Parameters and modulation

(defprotocol Modulator
  "A protocol for controlling AudioParams"
  (set-param! [param modulator]))

(extend-protocol Modulator
  number
  (set-param! [n param] (set! (.-value param) n))
  cljs.core.async.impl.channels.ManyToManyChannel
  (set-param! [c param]
    (go (while true
      (let [x (<! c)]
        (set-param! param x))))))

(defn schedule-value!
  "Ramps the parameter to the value at the given time from now"
  [param value time]
  (if (= value 0)
    (.exponentialRampToValueAtTime param 0.00001 time)
    (.exponentialRampToValueAtTime param value time)))

(defn cancel-scheduled-values!
  "Cancels scheduled values but maintains the current value"
  [ctx param]
  (let [current (.-value param)]
    (.cancelScheduledValues param (now ctx))
    (.setValueAtTime param current (now ctx))
    (schedule-value! param current (now ctx))))


(defn- apply-env [ctx levels times param]
  (println levels)
  (cancel-scheduled-values! ctx param)
  (let [times (map #(+ (now ctx) %) (reductions + times))]
    (js/console.log (clj->js times))
    (js/console.log (now ctx))
    (dorun (map #(schedule-value! param %1 %2) levels times))))


(defrecord EnvGen [ctx env gate]
  Modulator
  (set-param! [this param]
    (set-param! 0 param)
    (go (while true
      (let [g (<! gate)]
        (if (> g 0)
          (apply-env ctx
                     (map (partial * g) (:on-levels env))
                     (:on-times env)
                     param)
          (apply-env ctx
                     (:off-levels env)
                     (:off-times env)
                     param)))))))

(defrecord CV [value node]
  Modulator
  (set-param! [this param]
    (set-param! value param)
    (.connect node param)))



;; Buffers and samples

(defn decode-audio [ctx data]
  (.decodeAudioData ctx data))

(defn audio-buffer
  [ctx channels length sample-rate]
  (.createBuffer ctx channels length sample-rate))



;;; AnalyserNode methods

(defn float-freq-data [analyser] (.getFloatFrequencyData analyser))
(defn byte-freq-data [analyser] (.getByteFrequencyData analyser))
(defn float-time-domain-data [analyser] (.getFloatTimeDomainData analyser))
(defn byte-time-domain-data [analyser] (.getByteTimeDomainData analyser))


;;; AudioNode wrappers

(defn analyser
  [ctx in]
  (let [analyser-node (.createAnalyser ctx)]
    (.connect in analyser-node)
    analyser-node))

(defn biquad-filter
  [ctx in type freq q]
  (let [filter-node (.createBiquadFilter ctx)]
    (set! (.-type filter-node) (clj->js type))
    (set-param! freq (.-frequency filter-node))
    (set-param! q (.-Q filter-node))
    (.connect in filter-node)
    filter-node))

(defn convolver
  [ctx in buffer normalize]
  (let [convolver-node (.createConvolver ctx)]
    (set! (.-buffer convolver-node) buffer)
    (set! (.-normalize convolver-node) normalize)
    convolver-node))

(defn sig-delay
  [ctx in time]
  (let [delay-node (.createDelay ctx 5)]
    (set-param! time (.-delayTime delay-node))
    (.connect in delay-node)
    delay-node))

(defn compressor
  [ctx in threshold knee ratio reduction attack release]
  (let [compressor-node (.createDynamicsCompressor ctx)]
    (set-param! threshold (.-threshold compressor-node))
    (set-param! knee (.-knee compressor-node))
    (set-param! ratio (.-ratio compressor-node))
    (set! (.-reduction compressor-node) reduction)
    (set-param! attack (.-attack compressor-node))
    (set-param! release (.-release compressor-node))
    compressor-node))

(defn gain
  [ctx in amount]
  (let [gain-node (.createGain ctx)]
    (set-param! amount (.-gain gain-node))
    (.connect in gain-node)
    gain-node))

(defn media-element-source [ctx elem]
  (.createMediaElementSource ctx elem))

(defn media-stream-destination
  [ctx in]
  (let [dest-node (.createMediaStreamDestination ctx)]
    (.connect in dest-node)
    dest-node))

(defn media-stream-source [ctx stream]
  (.createMediaStreamSource ctx stream))

(defn oscillator
  [ctx type freq detune]
  (let [osc-node (.createOscillator ctx)]
    (set! (.-type osc-node) (clj->js type))
    (set-param! freq (.-frequency osc-node))
    (set-param! detune (.-detune osc-node))
    (.start osc-node)
    osc-node))

(defn periodic-wave
  [ctx real imag]
  (.createPeriodicWave ctx real imag))

(defn stereo-panner
  [ctx in pan]
  (let [panner-node (.createStereoPanner ctx)]
    (set-param! (.-pan panner-node) pan)
    (.connect in panner-node)
    panner-node))

(defn waveshaper
  [ctx in curve]
  (let [waveshaper-node (.createWaveShaper ctx)]
    (set! (.-curve waveshaper-node) curve)
    (.connect in waveshaper-node)
    waveshaper-node))

(defn buffer-source
  [ctx buffer detune loop]
  (let [source-node (.createBufferSource ctx)]
    (set! (.-buffer source-node) buffer)
    (.start source-node)
    source-node))

;;; NOT CORE

(defn mix
  "Combines the input AudioNodes into a GainNode"
  [ctx & ins]
  (let [mixed (.createGain ctx)]
    (doseq [in ins]
      (.connect in mixed))
    mixed))

(defn fb
  "f is a function that takes audio"
  [ctx in f]
  (let [gain-node (.createGain ctx)]
    (.connect in gain-node)
    (.connect (f gain-node) gain-node)
    gain-node))
