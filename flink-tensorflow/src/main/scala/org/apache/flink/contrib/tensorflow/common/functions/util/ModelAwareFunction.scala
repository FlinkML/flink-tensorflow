package org.apache.flink.contrib.tensorflow.common.functions.util

import org.apache.flink.api.common.functions.RichFunction
import org.apache.flink.configuration.Configuration
import org.apache.flink.contrib.tensorflow.models.Model

/**
  * A mix-in for functions to support the model lifecycle.
  */
trait ModelAwareFunction {
  this: RichFunction =>

  def model: Model[_]

  override def open(parameters: Configuration): Unit = ModelUtils.openModel(model)

  override def close(): Unit = ModelUtils.closeModel(model)

}
