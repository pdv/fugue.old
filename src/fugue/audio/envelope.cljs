(ns fugue.audio.envelope)

;; bias is offset
;; (envelope, gate, levelScale, levelBias, timeScale, doneAction)

(defn env-gen
  ([env gate] (env-gen env gate 1 0 1))
  ([env gate scale bias t-scale]
   ()))

;; if gate:
;;     ramp from 0 to scale in env.a time
;;     ramp from scale to env.s in env.d time
