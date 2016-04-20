(ns fugue.midi)

(defonce ins (atom {}))
(defonce outs (atom {}))


(defn note->hz [note]
  (* 440.0 (js/Math.pow 2.0 (/ (- note 69.0) 12.0))))

(defn deref-vals
  "Returns the input atom map with its values deref'd"
  [am]
  (into {} (for [[k a] am] [k (deref a)])))

(defn reset-vals!
  "Updates the atom map with new values"
  [am new]
  (for [[k v] new] (reset! (k am) v)))

(defn mono-update
  "Updates the cv map based on the input message"
  [cvm msg]
  (let [velo (/ (:velocity msg) 127.0)
        note (:note msg)
        prev (:active cvm)
        active (if (= velo 0)
                 (filter (partial = note) prev)
                 (conj prev note))]
    {:active active
     :freq (note->hz (last active))
     :gate (if (= 1 (count active)) velo (:gate cvm))}))

(defn mono-onmsg
  "Legato (no retrigger), last-note priority monophony"
  [cvg msg]
  (let [current (deref-vals cvg)
        new (mono-update current msg)]
    (reset-vals! cvg new)))

(defn midi-mono
  "Returns a control voltage map of active-notes/freq/velogate atoms"
  [name]
  (let [cvg {:active-notes (atom [])
             :freq (atom 440)
             :gate (atom 0)}]
    (add-watch ins :midi-mono #(mono-onmsg cvg (%4 name)))
    cvg))

(defn midi-in
  "Returns an up-to-date map of freq and velogate atoms"
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
  {144 :note-on
   128 :note-off
   224 :bend})

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
    (listen in)))

(defn init!
  "Initializes midi I/O"
  []
  (.. (.requestMIDIAccess js/navigator)
      (then open-ports)))
