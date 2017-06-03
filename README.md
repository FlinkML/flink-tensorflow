# flink-tensorflow

[![Build Status](https://api.travis-ci.org/cookieai/flink-tensorflow.png?branch=master)](https://travis-ci.org/cookieai/flink-tensorflow)
[![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/flink-tensorflow/Lobby)

Welcome!  **flink-tensorflow** is a library for machine intelligence in [Apache Flink™](http://flink.apache.org/),
using the [TensorFlow](https://www.tensorflow.org/) library and associated models.

Want to learn more? [See the wiki](https://github.com/cookieai/flink-tensorflow/wiki).

Help wanted!  See [the project board](https://github.com/cookieai/flink-tensorflow/projects) and [the issues section](https://github.com/cookieai/flink-tensorflow/issues) for specifics.    Feel free to join [the chat channel](https://gitter.im/flink-tensorflow/Lobby).

## Installation process for Maven and SBT (Local) :

- Clone repository (Run on Terminal)
```
git clone https://github.com/FlinkML/flink-tensorflow.git
```

- Publish it in local maven repository (Run on Terminal)
```
mvn clean install
```
Than follow :

### For SBT 
Add dependency in build.sbt
```
libraryDependencies += "ai.cookie" %% "flink-tensorflow" % "0.1-SNAPSHOT"
```

**Note:**  Make sure you have local maven resolver defined in build.sbt ```resolvers in ThisBuild ++= Seq(Resolver.mavenLocal)``` and ```scalaVersion in ThisBuild := "2.11.*"``` by default build has Scala 2.11 dependency. If you wants you can modify it in pom.xml and rebuild with different Scala version.


### For Maven
Add dependency in pom.xml
```
<dependency>
	<groupId>ai.cookie</groupId>
	<artifactId>flink-tensorflow_2.11</artifactId>
	<version>0.1-SNAPSHOT</version>
</dependency>
```


## Disclaimer
Apache®, Apache Flink™, Flink™, and the Apache feather logo are trademarks of [The Apache Software Foundation](http://apache.org).

TensorFlow, the TensorFlow logo and any related marks are trademarks of Google Inc.
