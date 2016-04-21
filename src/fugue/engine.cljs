(ns fugue.engine)


;; AudioContext

(defn make-ctx [] (js/AudioContext.))

(defn current-time [ctx]
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


;;; AudioParam

(defprotocol Modulator
  "A protocol for controlling AudioParams"
  (attach [this ctx param]))

(extend-protocol Modulator
  number
  (attach [n ctx param] (set! (.-value param) n))
  Atom
  (attach [a ctx param]
    (add-watch a :modulator #(set! (.-value param) %4)))
  cljs.core.PersistentArrayMap
  (attach [m ctx param]
    (set! (.-value param) (:value m))
    (.connect (:node m) param)))


(defn schedule-value!
  "Ramps the parameter to the value at the given time."
  [param value time]
  (if (= value 0)
    (do ; You can't exponential ramp to 0
      (.exponentialRampToValueAtTime param 0.00001 time))
      ; (.setValueAtTime param 0 time))
    (.exponentialRampToValueAtTime param value time)))

(defn cancel-scheduled-values!
  "Cancels scheduled values but maintains the current value"
  [param time]
  (js/console.log "Canceling")
  (.exponentialRampToValueAtTime param (+ (.-value param) 0.0001) time))


;; env is a function that takes the current time and gate
;; and returns a list of {:value :time} maps
(defrecord EnvGen [env gate]
  Modulator
  (attach [this ctx param]
    (set! (.-value param) 0)
    (schedule-value! param 0 0)
    (add-watch gate :gate
               #(doseq [{:keys [value time]} (env %4 (current-time ctx))]
                  (schedule-value! param value time)))))


;; ;; core.async.impl.channels/ManyToManyChannel
;; (def chan-type (type (chan)))

;; ;; {:time from previous (immediately if ommitted)
;; ;;  :value to set param to (cancel scheduled if ommitted)}
;; (extend-type chan-type
;;   Modulator
;;   (attach [ch ctx param]
;;     (js/console.log "connecting")
;;     (set! (.-value param) 0)
;;     (schedule-value! param 0 0)
;;     (go (loop [previous (current-time ctx)]
;;       (let [{:keys [time value]} (<! ch)]
;;         (if time
;;           (let [end-time (+ previous time)]
;;             (schedule-value! param value end-time)
;;             (recur end-time))
;;           (do ; cannot set directly or future scheduling will not work
;;             (cancel-scheduled-values! param 0) ; is this necessary?
;;             (when value
;;               (js/console.log "Instant change to value" value)
;;               (schedule-value! param value (current-time ctx)))
;;             (recur (current-time ctx)))))))))



;;; AudioBuffer

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


;;; AudioNode

(defn analyser
  [ctx in]
  (let [analyser-node (.createAnalyser ctx)]
    (.connect in analyser-node)
    analyser-node))

(defn biquad-filter
  ([ctx in type freq q]
   (let [filter-node (.createBiquadFilter ctx)]
     (set! (.-type filter-node) (clj->js type))
     (attach freq ctx (.-frequency filter-node))
     (attach q ctx (.-Q filter-node))
     (.connect in filter-node)
     filter-node)))

(defn convolver
  [ctx in buffer normalize]
  (let [convolver-node (.createConvolver ctx)]
    (set! (.-buffer convolver-node) buffer)
    (set! (.-normalize convolver-node) normalize)
    convolver-node))

(defn sig-delay
  [ctx in time]
  (let [delay-node (.createDelay ctx 5)]
    (attach time ctx (.-delayTime delay-node))
    (.connect in delay-node)
    delay-node))

(defn compressor
  [ctx in threshold knee ratio reduction attack release]
  (let [compressor-node (.createDynamicsCompressor ctx)]
    (attach threshold ctx (.-threshold compressor-node))
    (attach knee ctx (.-knee compressor-node))
    (attach ratio ctx (.-ratio compressor-node))
    (set! (.-reduction compressor-node) reduction)
    (attach attack ctx (.-attack compressor-node))
    (attach release ctx (.-release compressor-node))
    compressor-node))

(defn gain
  [ctx in amount]
  (let [gain-node (.createGain ctx)]
    (attach amount ctx (.-gain gain-node))
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
    (attach freq ctx (.-frequency osc-node))
    (attach detune ctx (.-detune osc-node))
    (.start osc-node)
    osc-node))

(defn periodic-wave
  [ctx real imag]
  (.createPeriodicWave ctx real imag))

(defn stereo-panner
  [ctx in pan]
  (let [panner-node (.createStereoPanner ctx)]
    (attach pan ctx (.-pan panner-node))
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
  [ctx ins]
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
