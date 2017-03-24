package org.apache.flink.contrib.tensorflow.types

/**
  * A qualified tensor name.
  */
case class TensorName private (name: String, index: Int) {
  override def toString: String = s"$name:$index"
}

object TensorName {
  /**
    * Create a tensor name.
    */
  def apply(str: String): TensorName = {
    val c = str.split(':')
    require(c.length <= 2, s"The value $str is not a valid tensor name. " +
      "Tensor names must be of the form \"<op_name>:<output_index>\".")
    new TensorName(c.head, if(c.length == 2) c.apply(1).toInt else 0)
  }
}
