package org.apache.flink.contrib.tensorflow.common.functions.util

import org.apache.flink.contrib.tensorflow.models.{Model, RichModel}

/**
  * Utility methods for TensorFlow models.
  */
object ModelUtils {
  /**
    * Open a model.
    */
  @throws(classOf[Exception])
  def openModel(model: Model[_]): Unit = {
    model match {
      case m: RichModel[_] => m.open()
    }
  }

  /**
    * Close a model.
    */
  @throws(classOf[Exception])
  def closeModel(model: Model[_]): Unit = {
    model match {
      case m: RichModel[_] => m.close()
    }
  }
}
