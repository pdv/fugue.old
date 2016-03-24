(ns fugue.engine.ctx)

(defonce ctx* (atom (js/AudioContext.)))

(defn reload!
  "Close the current context and replace it with a new one"
  []
  (.close @ctx*)
  (reset! ctx* (js/AudioContext.)))

(defn current-time []
  (.-currentTime @ctx*))

(defn out [in]
  (.connect in (.-destination @ctx*))
  in)

(defn sample-rate []
  (.-sampleRate @ctx*))

(defn create-biquad-filter []
  (.createBiquadFilter @ctx*))

(defn create-oscillator []
  (.createOscillator @ctx*))

(defn create-gain []
  (.createGain @ctx*))

(defn create-delay [max-delay]
  (.createDelay @ctx* max-delay))

(defn- float32-array
  "Converts a sequence to a Float32Array"
  [seq]
  (.from js/Float32Array (clj->js seq)))

(defn periodic-wave [real imag]
  (let [real-arr (float32-array real)
        imag-arr (float32-array imag)
        wave (.createPeriodicWave @ctx* real-arr imag-arr)
        osc  (.createOscillator @ctx*)]
    (.setPeriodicWave osc wave)
    osc))

(defn buffer [size f]
  (let [buffer-size (* 2 (sample-rate))
        buffer (.createBuffer @ctx* 1 buffer-size (sample-rate))
        buffer-arr (.getChannelData buffer 0)]
    (doseq [i (range buffer-size)]
      (aset buffer-arr i (f i)))
    buffer))

(defn buffer-source
  ([buffer] (buffer-source buffer false))
  ([buffer loop]
   (let [source-node (.createBufferSource @ctx*)]
     (set! (.-buffer source-node) buffer)
     (set! (.-loop source-node) loop)
     source-node)))

(def one
  (let [buffer-size (* 2 (sample-rate))
        buffer (.createBuffer @ctx* 1 buffer-size (sample-rate))
        buffer-arr (.getChannelData buffer 0)
        buffer-source (.createBufferSource @ctx*)]
    (doseq [i (range buffer-size)]
      (aset buffer-arr i (- (.random js/Math) 1)))
    (set! (.-buffer buffer-source) buffer)
    (set! (.-loop buffer-source) true)
    (.start buffer-source)
    buffer-source))

(comment
  (js/console.log one) 
)
