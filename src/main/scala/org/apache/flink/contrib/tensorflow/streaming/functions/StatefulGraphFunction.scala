package org.apache.flink.contrib.tensorflow.streaming.functions

import org.apache.flink.api.common.functions.RichFunction
import org.apache.flink.api.common.state.{ValueState, ValueStateDescriptor}
import org.apache.flink.configuration.Configuration
import org.apache.flink.contrib.tensorflow.common.GraphSerializer
import org.tensorflow.Graph

/**
  * Trait implementing the functionality necessary to apply stateful graph functions.
  */
trait StatefulGraphFunction[I,O] extends RichFunction {

  private[this] var state: ValueState[Graph] = _

  def applyWithState(in: I, fun: (I, Graph) => O): O = {
    var graph = state.value()
    if(graph == null) {
      // initialize the graph
      graph = new Graph()
    }
    val o = fun(in, graph)
    state.update(graph)
    o
  }

  override def open(parameters: Configuration): Unit = {
    val info = new ValueStateDescriptor[Graph]("graph-state", GraphSerializer.INSTANCE, null.asInstanceOf[Graph])
    state = getRuntimeContext().getState(info)
  }
}
