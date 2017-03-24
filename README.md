# flink-tensorflow


## Building

### Build TensorFlow

Please refer to TensorFlow for general instructions.

Additional steps are required to use the new TensorFlow Java library.  Here's how to
build and install the library to the local Maven repository:

```sh
$ cd $TENSORFLOW_HOME
$ bazel build //tensorflow/java:tensorflow //tensorflow/java:pom
mvn install:install-file -Dfile=bazel-bin/tensorflow/java/libtensorflow.jar -DpomFile=bazel-bin/tensorflow/java/pom.xml
```

### Run Tests
The project uses JUnit and the Maven Surefire plugin.  Be sure to add the
the native tensorflow library to the Java native library path.

To run the tests:
 
```sh
$ mvn surefire:test -DargLine="-Djava.library.path=$TF_HOME/bazel-bin/tensorflow/java"
```

