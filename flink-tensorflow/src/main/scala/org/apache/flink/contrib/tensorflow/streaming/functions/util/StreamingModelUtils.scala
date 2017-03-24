package org.apache.flink.contrib.tensorflow.streaming.functions.util

import org.apache.flink.contrib.tensorflow.models.Model
import org.apache.flink.contrib.tensorflow.streaming.models.CheckpointedModel
import org.apache.flink.runtime.state._

/**
  * Utility methods to support model checkpointing.
  */
object StreamingModelUtils {
  def snapshotModelState(context: ManagedSnapshotContext, model: Model[_]): Unit = {
    model match {
      case m: CheckpointedModel => m.snapshotState(context)
    }
  }

  def restoreModelState(context: ManagedInitializationContext, model: Model[_]): Unit = {
    model match {
      case m: CheckpointedModel => m.initializeState(context)
    }
  }
}
