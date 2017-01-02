package org.apache.flink.contrib.tensorflow.streaming.operator

import org.apache.flink.api.common.functions.{RichFunction, RuntimeContext}
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.checkpoint.CheckpointedFunction
import org.tensorflow.Graph

/**
  * @author Eron Wright
  */
trait SessionOperatorContext extends RichFunction {

  // the requisite graph
  def graph: Graph

  abstract override def open(parameters: Configuration): Unit = {
    super.open(parameters)

    // initialize the session
  }
}
