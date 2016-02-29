(ns fugue.engine.ctx)

(defonce ctx* (atom (js/AudioContext.)))

(defn reload!
  "Close the current context and replace it with a new one"
  []
  (.close @ctx*)
  (reset! ctx* (js/AudioContext.)))

(defn current-time []
  (.-currentTime @ctx*))

(defn out [output]
  (.connect output (.-destination @ctx*)))

(defn sample-rate []
  (.-sampleRate @ctx*))

(defn create-biquad-filter []
  (.createBiquadFilter @ctx*))

(defn create-oscillator []
  (.createOscillator @ctx*))

(defn create-gain []
  (.createGain @ctx*))

(defn create-delay []
  (.createDelay @ctx*))
