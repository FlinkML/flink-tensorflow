package org.apache.flink.contrib.tensorflow.streaming.functions

import org.apache.flink.contrib.tensorflow.common.functions.util.ModelAwareFunction
import org.apache.flink.contrib.tensorflow.streaming.functions.util.CheckpointedModelAwareFunction
import org.apache.flink.streaming.api.checkpoint.CheckpointedFunction
import org.apache.flink.streaming.api.scala.function.RichAllWindowFunction
import org.apache.flink.streaming.api.windowing.windows.Window

/**
  * An abstract all-window function with TensorFlow model support.
  */
abstract class AbstractAllWindowFunction[IN, OUT, W <: Window] extends RichAllWindowFunction[IN, OUT, W]
  with CheckpointedFunction
  with ModelAwareFunction
  with CheckpointedModelAwareFunction {

}
