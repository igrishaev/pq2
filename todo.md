- organize tests
- move cpp files into a directory
- ci: fix windows x64 build
- ci: mute windows arm build

- add dockerfile with postgres instances
- tests: fixture to run against all dbs

- client: toString
- client: get user password host port etc
- client: get params
- client: get default params
- client: try ssl

- cpp: debug macro & build parameter
- statement: toString
- result toString
- result rename isMulti
- result static constructor (nTuples cache?)
- result calculate nColumns once
- result asArray
- result asMap

- connection execute
- connection executeChunked
- connection cancel
- connection secret
- connection pid

// TODO: reset status
// TODO: move enums
// TODO: rename enum

- move postgres lib to test deps
- move benchmarks to another package
