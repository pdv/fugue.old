(ns fugue.midi
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :refer [chan >!]]))

(defonce midi-ins (atom {}))
(defonce midi-outs (atom {}))

(defn midi-in [name]
  (@midi-ins name))

(defn midi-out [name]
  (@midi-outs name))

(defn midi-in-devices []
  (keys @midi-ins))

(defn midi-out-devices []
  (keys @midi-outs))

(defn printer
  [in]
  (go (while true (.log js/console (clj->js (<! in))))))

(def msg-type
  {144 :note-on
   128 :note-off
   224 :bend})

(defn arr->msg
  "Turns a Uint8Array message into a map"
  [arr]
  (let [[status note velocity] (js->clj (.from js/Array arr))]
    {:type (if (= 0 velocity)
             :note-off
             (msg-type (bit-and status 0xf0)))
     :note note
     :velocity velocity}))

(defn msg->arr
  "Turns a midi message into a js array"
  [msg]
  (clj->js [144 (:note msg) (:velocity msg)]))

(defn in->chan
  "Turns a MIDIInput into an async channel"
  [midi-input]
  (let [c (chan)]
    (set! (.-onmidimessage midi-input)
          #(go (>! c (arr->msg (.-data %)))))
    c))

(defn out->chan
  "Turns a MIDIOutput into a receiver channel"
  [midi-output]
  (let [c (chan)]
    (go (while true (.send midi-output (msg->arr (<! c)))))
    c))

(defn port-map
  "Returns a name-channel map from a seq of MidiPorts"
  [port->chan ports]
  (apply merge (map #(hash-map (.-name %) (port->chan %)) ports)))

(defn maplike->seq
  "The Web MIDI Api uses 'maplike' for its MIDIInputMap and MIDIOutputMap"
  [m]
  (js->clj (.from js/Array (.values m))))

(defn reset-ports! [midi-access]
  (reset! midi-ins (port-map in->chan (maplike->seq (.-inputs midi-access))))
  (reset! midi-outs (port-map out->chan (maplike->seq (.-outputs midi-access)))))

(defn midi-init! []
  "Initializes midi io"
  (.. (.requestMIDIAccess js/navigator)
      (then reset-ports!)))

(comment
(defn midi-inst
  "Returns an instrument ()"
  [f]
  (fn [midi-in]
    (go
      (loop [gates {}
             msg (<! midi-in)]
        (if (= (:type msg) :note-on)
          (let [gate (atom 1)]
            )
          )
      (while true
          (let [msg (<! in)]
            (if (= (:type msg) :note-on)
              ())
          ()(<! in))))
))))
