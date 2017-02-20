package org.apache.flink.contrib.tensorflow.streaming.functions

import org.apache.flink.contrib.tensorflow.common.functions.util.ModelFunctionSupport
import org.apache.flink.contrib.tensorflow.streaming.functions.util.StreamingModelSupport
import org.apache.flink.streaming.api.checkpoint.CheckpointedFunction
import org.apache.flink.streaming.api.functions.co.RichCoProcessFunction

/**
  */
abstract class AbstractCoProcessFunction[IN1, IN2, OUT] extends RichCoProcessFunction[IN1, IN2, OUT]
  with CheckpointedFunction
  with ModelFunctionSupport
  with StreamingModelSupport {

}
