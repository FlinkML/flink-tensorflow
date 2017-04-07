package org.apache.flink.contrib.tensorflow.streaming.functions

import org.apache.flink.contrib.tensorflow.common.functions.util.ModelAwareFunction
import org.apache.flink.contrib.tensorflow.streaming.functions.util.CheckpointedModelAwareFunction
import org.apache.flink.streaming.api.checkpoint.CheckpointedFunction
import org.apache.flink.streaming.api.scala.function.RichWindowFunction
import org.apache.flink.streaming.api.windowing.windows.Window

/**
  * An abstract keyed window function with TensorFlow model support.
  */
abstract class AbstractWindowFunction[IN, OUT, KEY, W <: Window] extends RichWindowFunction[IN, OUT, KEY, W]
  with CheckpointedFunction
  with ModelAwareFunction
  with CheckpointedModelAwareFunction {

}
