# fugue

Programmable music on the web.

## Usage

To compile:
`lein cljsbuild once`
Build on changes
`lein cljsbuild auto`


Don't use go-loop. It doesn't work right in cljs.
(go (loop ...)) and (go (while true ...)) work fine

## License

Copyright © 2016 Philip Del Vecchio

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
