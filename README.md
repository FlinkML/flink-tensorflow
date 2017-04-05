# flink-tensorflow

[![Build Status](https://api.travis-ci.org/cookieai/flink-tensorflow.png?branch=master)](https://travis-ci.org/cookieai/flink-tensorflow)
[![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/flink-tensorflow/Lobby)

## Building

## Build Example
A set of examples are provided as a ready-to-run JAR file.   To build:
```sh
$ mvn clean package
```

The build file is located at: `flink-tensorflow-examples/target/flink-tensorflow-examples_2.11-0.1-SNAPSHOT.jar`

### Run Tests
The project uses JUnit and the Maven Surefire plugin.  Be sure to add the
the native tensorflow library to the Java native library path.

To run the tests:
 
```sh
$ mvn surefire:test
```

## Running the Examples
_Prerequisite: install and run Apache Flink 1.2._

### Inception
1. Download the inception5h model.
2. Launch the inception demo:
```
$ flink run -c org.apache.flink.contrib.tensorflow.examples.inception.Inception flink-tensorflow-examples/target/flink-tensorflow-examples_2.11-0.1-SNAPSHOT.jar <path-to-inception5h> <path-to-image-files>
```
