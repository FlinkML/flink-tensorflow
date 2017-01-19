package org.apache.flink.contrib.tensorflow.models

import org.apache.flink.contrib.tensorflow.models.Model.RunContext
import org.apache.flink.ml.common.ParameterMap
import org.tensorflow.Session

import scala.annotation.implicitNotFound

/**
  * Type class for the run operation of a [[Model]].
  *
  * The [[Signature]] contains a self type parameter so that the Scala compiler looks into
  * the companion object of this class to find implicit values.
  *
  * @tparam Self Type of the [[Model]] subclass to which the [[Signature]] applies.
  * @tparam IN   Type of the input data
  */
@implicitNotFound(msg = "Cannot find Signature type class for model ${Self} and input ${IN}")
trait Signature[Self, IN] extends Serializable {
  type OUT

  def run(instance: Self, context: RunContext, input: IN): OUT
}
