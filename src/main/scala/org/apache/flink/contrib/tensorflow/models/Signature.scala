package org.apache.flink.contrib.tensorflow.models

import org.apache.flink.contrib.tensorflow.models.Model.RunContext
import org.apache.flink.ml.common.ParameterMap
import org.tensorflow.Session
import org.tensorflow.framework.SignatureDef

import scala.annotation.implicitNotFound

/**
  * Type class for the run operation of a [[Model]].
  *
  * The [[Signature]] contains a self type parameter so that the Scala compiler looks into
  * the companion object of this class to find implicit values.
  *
  * @tparam Self Type of the [[Model]] subclass to which the [[Signature]] applies.
  */
@implicitNotFound(msg = "Cannot find signature for model ${Self}")
trait Signature[Self] extends Serializable {
}
