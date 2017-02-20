package org.apache.flink.contrib.tensorflow.streaming.functions

import org.apache.flink.contrib.tensorflow.common.functions.util.ModelFunctionSupport
import org.apache.flink.contrib.tensorflow.streaming.functions.util.StreamingModelSupport
import org.apache.flink.streaming.api.checkpoint.CheckpointedFunction
import org.apache.flink.streaming.api.functions.RichProcessFunction

/**
  */
abstract class AbstractProcessFunction[I, O] extends RichProcessFunction[I, O]
  with CheckpointedFunction
  with ModelFunctionSupport
  with StreamingModelSupport {

}
