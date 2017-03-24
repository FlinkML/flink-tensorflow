package org.apache.flink.contrib.tensorflow.common.io

import org.apache.flink.api.common.io.RichInputFormat
import org.apache.flink.contrib.tensorflow.common.functions.util.ModelUtils
import org.apache.flink.contrib.tensorflow.models.Model
import org.apache.flink.core.io.InputSplit

/**
  * A stackable trait for input formats to support the model lifecycle.
  */
@deprecated("unused")
trait InputFormatModelOperations[OT, T <: InputSplit] extends RichInputFormat[OT,T] {

  def model: Model[_]

  abstract override def openInputFormat(): Unit = {
    super.openInputFormat()
    ModelUtils.openModel(model)
  }

  abstract override def closeInputFormat(): Unit = {
    ModelUtils.closeModel(model)
    super.closeInputFormat()
  }
}
