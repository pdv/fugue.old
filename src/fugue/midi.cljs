(ns fugue.midi
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :refer [chan >!]]))

(defonce midi-ins (atom {}))

(defn midi-in [name]
  (@midi-ins name))

(defn midi-devices []
  (keys @midi-ins))

(def msg-type
  {144 :note-on
   128 :note-off
   224 :bend})

(defn arr->msg
  "Turns a Uint8Array message into a map"
  [arr]
  (let [[status note velocity] (js->clj arr)]
    {:type (msg-type (bit-and status 0xf0))
     :note note
     :velocity velocity}))

(defn in->chan
  "Turns a MIDIInput into an async channel"
  [midi-input]
  (let [c (chan)]
    (set! (.-onmidimessage midi-input)
          #((go (>! c (arr->msg (.-data %))))))
    c))

(defn input-map
  "Creates a name-channel map for the inputs from midi-access"
  [midi-access]
  (let [inputs (.values (.-inputs midi-access))]
    (loop [input-map {}
           input (.next inputs)]
      (.log js/console input)
      (if (.-done input)
        input-map
        (recur (assoc input-map
                      (.. input -value -name)
                      (in->chan (.-value input)))
               (.next inputs))))))

(defn midi-init []
  (.. (.requestMIDIAccess js/navigator)
      (then #(reset! midi-ins (input-map %)))))

