package org.apache.flink.contrib.tensorflow.common.functions

import org.apache.flink.api.common.functions._
import org.apache.flink.contrib.tensorflow.common.functions.util.ModelAwareFunction

/**
  * An abstract [[FlatMapFunction]] with TensorFlow model support.
  *
  * This function does not support checkpointing.
  * Consider using [[org.apache.flink.contrib.tensorflow.streaming.functions.AbstractProcessFunction]].
  */
abstract class AbstractFlatMapFunction[IN,OUT] extends RichFlatMapFunction[IN,OUT]
  with ModelAwareFunction {
}
