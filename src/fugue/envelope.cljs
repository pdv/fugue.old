(ns fugue.envelope
  (:require [fugue.engine :as engine]))

(defn adsr [a d s r]
  {:levels [0 1 s 0]
   :times [a d r]
   :release 2})

(defn perc [a r]
  {:levels [0 1 0]
   :times [a r]
   :release 2})

(defn- apply-env [levels times n param gate]
  (engine/cancel-scheduled-values! param)
  (let [slice (partial (if (> gate 0) take drop) n)
        levels (slice levels)
        times (map #(+ (engine/now) %) (reductions + (slice times)))]
    (dorun (map #(engine/schedule-value! param %1 %2) levels times))))

(defn env-gen
  "Creates an envelope generator triggered by a gate"
  ([env gate] (env-gen env gate 1))
  ([env gate scale] (env-gen env gate scale 0))
  ([env gate scale bias]
   (let [levels (map #(+ bias (* scale %)) (:levels env))
         applicator (partial apply-env (rest levels) (:times env) (:release env))]
     (fn [param]
       (engine/set-param! param (first levels))
       (applicator param @gate)
       (add-watch gate :gate #(applicator param %4))))))
