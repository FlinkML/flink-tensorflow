package org.apache.flink.contrib.tensorflow.common.functions.util

import org.apache.flink.contrib.tensorflow.models.{Model, RichModel}

/**
  */
object ModelUtils {
  @throws(classOf[Exception])
  def openModel(model: Model[_]): Unit = {
    model match {
      case m: RichModel[_] => m.open()
    }
  }

  @throws(classOf[Exception])
  def closeModel(model: Model[_]): Unit = {
    model match {
      case m: RichModel[_] => m.close()
    }
  }
}
