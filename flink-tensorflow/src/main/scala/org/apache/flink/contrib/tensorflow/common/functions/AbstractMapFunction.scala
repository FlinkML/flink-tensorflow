package org.apache.flink.contrib.tensorflow.common.functions

import org.apache.flink.api.common.functions._
import org.apache.flink.contrib.tensorflow.common.functions.util.FunctionModelOperations

/**
  * An abstract [[MapFunction]] with TensorFlow model support.
  *
  * This function does not support checkpointing.
  * Use [[org.apache.flink.contrib.tensorflow.streaming.functions.AbstractProcessFunction]] for that.
  */
abstract class AbstractMapFunction[IN,OUT] extends RichMapFunction[IN,OUT]
  with FunctionModelOperations {
}
