package org.apache.flink.contrib.tensorflow.graphs

import org.tensorflow.Tensor

/**
  * Defines a method supported by a TensorFlow graph.
  */
trait GraphMethod {

  /**
    * The input type of the method.
    */
  type Input

  /**
    * The output type of the method.
    */
  type Output

  /**
    * The method name.
    */
  def name: String

  /**
    * Gets the input values to feed when the method is invoked.
    */
  def inputs(in: Input): Map[String, Tensor]

  /**
    * Gets the result of invoking the method.
    * @param outputs a map of fetched outputs.
    */
  def outputs(outputs: Map[String, Tensor]): Output

}
