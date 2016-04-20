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
                 (filter (partial not= note) prev)
                 (conj prev note))]
    {:active active
     :freq (note->hz (last active))
     :gate (if (= 1 (count active)) velo (:gate cvm))}))

(defn mono-onmsg
  "Updates freq, gate, and active atoms based on msg"
  [msg freq gate active]
  (if (= :note (:type msg))
    (let [current {:freq @freq
                   :gate @gate
                   :active @active}
          new (mono-update current msg)]
      (js/console.log (clj->js new))
      (reset! freq (:freq new))
      (reset! gate (:gate new))
      (reset! active (:active new)))))

(defn midi-mono
  "Returns a control voltage map of active-notes/freq/velogate atoms"
  [name]
  (let [freq (atom 440)
        gate (atom 0)
        active (atom [])]
    (add-watch ins :midi-mono #(mono-onmsg (%4 name) freq gate active))
    {:freq freq :gate gate}))

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
  {144 :note
   128 :note
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
    (js/console.log (.-name in))
    (listen in)))

(defn init!
  "Initializes midi I/O"
  []
  (.. (.requestMIDIAccess js/navigator)
      (then open-ports)))
