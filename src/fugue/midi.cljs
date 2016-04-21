(ns fugue.midi)

(defonce ins (atom {}))
(defonce outs (atom {}))


(defn note->hz [note]
  (* 440.0 (js/Math.pow 2.0 (/ (- note 69.0) 12.0))))

(defn- update-mono
  "Updates the cv map based on the input message"
  [cvm msg]
  (let [velo (/ (:velocity msg) 127.0)
        note (:note msg)
        prev (:active cvm)
        active (if (= velo 0)
                 (filter (partial not= note) prev)
                 (conj prev note))]
    {:active active
     :freq (note->hz (first active))
     :gate (if (or (= 0 (count prev))
                   (= 0 (count active)))
             velo
             (:gate cvm))}))

(defn- mono-onmsg
  "Updates freq, gate, and active atoms based on msg"
  [msg freq gate active]
  (if (= :note (:type msg))
    (let [current {:freq @freq :gate @gate :active @active}
          new (update-mono current msg)]
      (if (and (not= (:freq new) (:freq current))
               (not= 0 (:gate new)))
        (reset! freq (:freq new)))
      (if (not= (:gate new) (:gate current))
        (reset! gate (:gate new)))
      (reset! active (:active new)))))

(defn midi-mono
  "Returns a control voltage map of active-notes/freq/velogate atoms"
  [name]
  (let [freq (atom 440)
        gate (atom 0)
        active (atom [])]
    (add-watch ins :midi-mono #(mono-onmsg (%4 name) freq gate active))
    {:freq freq :gate gate}))


(defn ctrl-onmsg
  "Updates atom a if the message is a control message"
  [msg a n]
  (if (and (= :ctrl (:type msg))
           (= n (:note msg)))
    (reset! a (:velocity msg))))

(defn midi-ctrl
  "Returns an atom representing the value of the midi control"
  [name n]
  (let [val (atom 0)]
    (add-watch ins :midi-ctl #(ctrl-onmsg (%4 name) val n))
    val))

(defn midi-in
  "DEPRECATED. Returns an up-to-date map of freq and velogate atoms"
  [name]
  (let [freq (atom 440)
        gate (atom 0)]
    (add-watch ins :midi-listener
               (fn [key atom old-state new-state]
                 (let [msg (new-state name)]
                   (reset! freq (note->hz (:note msg)))
                   (reset! gate (/ (:velocity msg) 127.0)))))
    {:freq freq :gate gate}))


;;; Initialization


(def msg-type
  {144 :note
   128 :note
   224 :bend
   176 :ctrl})

(defn event->msg
  "Converts a MIDIMessageEvent into a midi message"
  [e]
  (let [js-arr (.from js/Array (.-data e))
        [status note velocity] (js->clj js-arr)]
    {:type (msg-type (bit-and status 0xf0))
     :note note
     :velocity velocity}))

(defn- listen
  "Updates the ins atom when a midi message is received"
  [in]
  (let [name (.-name in)]
    (set! (.-onmidimessage in) #(swap! ins assoc name (event->msg %)))))

(defn- maplike->seq
  "The Web MIDI Api uses 'maplike' for its MIDIInputMap and MIDIOutputMap"
  [m]
  (js->clj (.from js/Array (.values m))))

(defn- open-ports [midi-access]
  (doseq [in (maplike->seq (.-inputs midi-access))]
    (js/console.log (.-name in))
    (listen in)))

(defn init!
  "Initializes midi I/O"
  []
  (.. (.requestMIDIAccess js/navigator)
      (then open-ports)))
