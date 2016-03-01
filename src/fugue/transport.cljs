(ns fugue.transport
  (:require [fugue.engine.ctx :as ctx]))

(defn stop []
  (ctx/reload!))

