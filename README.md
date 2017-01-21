# Goahead

Goahead is an AOT compiler for JVM classes targeting the Go runtime. It is a work in progress and not very usable in its
current state.

## Introduction

(TODO - several details are still being fleshed out)

## Usage

(TODO - several details are still being fleshed out)

## Building

This is built in Scala with SBT. Simply run any SBT command, e.g. `sbt package`.

## Building the Runtime

Download [Zulu JDK 9](http://zulu.org/zulu-9-pre-release-downloads/) and extract it. Set the `ZULU_JDK_HOME` environment
variable to the extracted path. Run the following:

    sbt buildRt

The resulting code will be written to `libs/java/rt` alongside the existing code prefixed with `aaa`.

## Testing

There is a light weight testing runtime that has to be built. To build it, perform the same sets as
"Building the Runtime" above except run this instead:

    sbt buildTestRt

Once a test RT is present, make sure that `GOPATH` is set and this repository is checked out at
`$GOPATH/src/github.com/cretz/goahead`. Then simply run:

    sbt test

## Licenses

All code including subdirectories, with the exception of the `libs/java` subdirectory, uses the MIT license (see
[LICENSE](LICENSE)). Since some of the work in `libs/java` is derivative of OpenJDK, it is licensed under the same
license as OpenJDK: GPL with the classpath exception. See [libs/java/LICENSE](libs/java/LICENSE) and
[libs/java/README.md](libs/java/README.md)