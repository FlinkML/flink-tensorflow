package org.apache.flink.contrib.tensorflow.streaming.functions.util

import org.apache.flink.contrib.tensorflow.models.Model
import org.apache.flink.runtime.state.{FunctionInitializationContext, FunctionSnapshotContext}
import org.apache.flink.streaming.api.checkpoint.CheckpointedFunction

/**
  * A mix-in for streaming functions to support model checkpointing.
  */
trait CheckpointedModelAwareFunction {
  this: CheckpointedFunction =>

  /**
    * The model to operate on.
    * @return
    */
  def model: Model[_]

  override def initializeState(context: FunctionInitializationContext): Unit =
    StreamingModelUtils.restoreModelState(context, model)

  override def snapshotState(context: FunctionSnapshotContext): Unit =
    StreamingModelUtils.snapshotModelState(context, model)
}
