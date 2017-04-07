package org.apache.flink.contrib.tensorflow.streaming.functions

import org.apache.flink.contrib.tensorflow.common.functions.util.ModelAwareFunction
import org.apache.flink.contrib.tensorflow.streaming.functions.util.CheckpointedModelAwareFunction
import org.apache.flink.streaming.api.checkpoint.CheckpointedFunction
import org.apache.flink.streaming.api.functions.RichProcessFunction

/**
  * An abstract process function with TensorFlow model support.
  */
abstract class AbstractProcessFunction[I, O] extends RichProcessFunction[I, O]
  with CheckpointedFunction
  with ModelAwareFunction
  with CheckpointedModelAwareFunction {

}
