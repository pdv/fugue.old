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

(defn env-gen
  "Creates an envelope generator triggered by a gate"
  ([env gate] (env-gen env gate 1))
  ([env gate scale] (env-gen env gate scale 0))
  ([env gate scale bias]
   (let [levels (map #(+ bias (* scale %)) (:levels env))
         n (:release env)
         on-levels (take n levels)
         off-levels (drop n levels)
         on-times (reductions + (cons 0 (take n (:times env))))
         off-times (reductions + (drop n (:times env)))]
     (letfn [(gate-change [param gate-old gate-new]
               (when (not (= gate-old gate-new))
                 (engine/cancel-scheduled-values! param)
                 (let [levels (if (= 0 gate-new) off-levels on-levels)
                       times (map #(+ (engine/now) %)
                                  (if (= 0 gate-new) off-times on-times))]
                   (map #(engine/schedule-value! param level time) levels times))))]
       (fn [param]
         (gate-change param 0 gate)
         (add-watch gate :gate #(gate-change param %3 %4)))))))
