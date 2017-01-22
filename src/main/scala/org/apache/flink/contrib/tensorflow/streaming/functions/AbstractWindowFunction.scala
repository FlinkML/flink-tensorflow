package org.apache.flink.contrib.tensorflow.streaming.functions

import org.apache.flink.contrib.tensorflow.common.functions.util.ModelSupport
import org.apache.flink.contrib.tensorflow.streaming.functions.util.StreamingModelSupport
import org.apache.flink.streaming.api.checkpoint.CheckpointedFunction
import org.apache.flink.streaming.api.scala.function.RichWindowFunction
import org.apache.flink.streaming.api.windowing.windows.Window

/**
  */
abstract class AbstractWindowFunction[IN, OUT, KEY, W <: Window] extends RichWindowFunction[IN, OUT, KEY, W]
  with CheckpointedFunction
  with ModelSupport
  with StreamingModelSupport {

}
