(ns fugue.transport
  (:require [fugue.engine :as engine]))

(defn stop []
  (engine/reload!))

