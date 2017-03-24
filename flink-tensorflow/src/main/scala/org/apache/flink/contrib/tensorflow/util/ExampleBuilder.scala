package org.apache.flink.contrib.tensorflow.util

import org.tensorflow.example.{Example, Feature, Features, FloatList}

import scala.collection.JavaConverters._

/**
  * Builds [[Example]] instances.
  */
object ExampleBuilder {

  /**
    * Produce an example with some features.
    */
  def example(values: (String,Feature)*): Example = {
    val features = Features.newBuilder().putAllFeature(Map(values:_*).asJava).build()
    Example.newBuilder().setFeatures(features).build()
  }

  /**
    * Produce a feature with some float values.
    */
  def feature(values: java.lang.Float*): Feature = {
    Feature.newBuilder()
      .setFloatList(FloatList.newBuilder.addAllValue(values.asJavaCollection))
      .build()
  }
}
