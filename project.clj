(defproject fugue "0.1.0-SNAPSHOT"
  :description "Programmable music on the web"
  :url "http://github.com/pdv/fugue"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.7.228"]
                 [com.cemerick/piggieback "0.2.1"]
                 [weasel "0.7.0"]]
  :plugins [[lein-cljsbuild "1.1.2"]]
  :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}
  :cljsbuild {
    :builds [{:source-paths ["src"]
              :compiler {:output-to "out/fugue.js"
                         :libs ["src/fugue/engine.js"]
                         :optimizations :whitespace
                         :pretty-print true}}]})


