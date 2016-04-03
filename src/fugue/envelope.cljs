(ns fugue.envelope
  (:require [fugue.engine :as engine]))

(defn adsr [a d s r]
  {:levels [0 1 s 0]
   :times [a d r]
   :release 3})

(defn perc [a r]
  {:levels [0 1 0]
   :times [a r]
   :release 3})

(defn env-gen
  "Creates an envelope generator triggered by a gate"
  ([env gate] (env-gen env gate 1))
  ([env gate scale] (env-gen env gate scale 0))
  ([env gate scale bias]
   (let [levels (map #(+ bias (* scale %)) (:levels env))
         n (:release env)
         on-levels (take n levels)
         off-levels (drop n levels)
         on-times (reductions + (take n (cons 0 (:times env))))
         off-times (reductions + (drop (- n 1) (:times env)))]
     (letfn [(gate-change [param gate-old gate-new]
               (when (not (= gate-old gate-new))
                 (engine/cancel-scheduled-values! param)
                 (let [levels (if (> 0 gate-new) on-levels off-levels)
                       times (map #(+ (engine/now) %)
                                  (if (> 0 gate-new) on-times off-times))]
                   (dorun (map #(engine/schedule-value! param %1 %2) levels times)))))]
       (fn [param]
         (gate-change param 0 @gate)
         (add-watch gate :gate #(gate-change param %3 %4)))))))
