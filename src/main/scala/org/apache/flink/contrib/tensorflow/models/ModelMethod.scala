package org.apache.flink.contrib.tensorflow.models

import org.tensorflow.Tensor

/**
  * Defines a method supported by a model.
  *
  * The [[ModelMethod]] type is base trait for types defining concrete model methods.
  *
  * A class implementing this trait acts as a magnet type for specific inputs and outputs.
  * The companion object provides implicit conversions (i.e. magnet branches) for
  * specific types that are convertible to/from tensors as required by the method.
  */
trait ModelMethod {

  /**
    * The method name.
    */
  def name: String

  /**
    * The result type of the method.
    */
  type Result

  /**
    * Gets the input values to feed when the method is invoked.
    */
  def inputs(): Map[String, Tensor]

  /**
    * Gets the result of invoking the method.
    * @param outputs a map of fetched outputs.
    */
  def outputs(outputs: Map[String, Tensor]): Result

}
