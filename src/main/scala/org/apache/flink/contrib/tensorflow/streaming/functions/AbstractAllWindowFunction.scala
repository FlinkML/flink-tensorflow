package org.apache.flink.contrib.tensorflow.streaming.functions

import org.apache.flink.contrib.tensorflow.common.functions.util.ModelFunctionSupport
import org.apache.flink.contrib.tensorflow.streaming.functions.util.StreamingModelSupport
import org.apache.flink.streaming.api.checkpoint.CheckpointedFunction
import org.apache.flink.streaming.api.scala.function.{RichAllWindowFunction, RichWindowFunction}
import org.apache.flink.streaming.api.windowing.windows.Window

/**
  */
abstract class AbstractAllWindowFunction[IN, OUT, W <: Window] extends RichAllWindowFunction[IN, OUT, W]
  with CheckpointedFunction
  with ModelFunctionSupport
  with StreamingModelSupport {

}
