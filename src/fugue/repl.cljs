(ns fugue.repl
  (:require [weasel.repl :as repl]))
(repl/connect "ws://localhost:9001")
