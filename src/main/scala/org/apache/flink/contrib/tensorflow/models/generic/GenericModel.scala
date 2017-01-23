package org.apache.flink.contrib.tensorflow.models.generic

import org.apache.flink.contrib.tensorflow.models.{Model, RichModel, Signature}
import org.apache.flink.util.Preconditions.checkState
import org.tensorflow.{Graph, Session}

/**
  * A generic model based on an ad-hoc graph.
  *
  * Implementation classes provide the graph to use with a graph loader.
  */
abstract class GenericModel[Self <: GenericModel[Self]] extends RichModel[Self] {
  that: Self =>

  protected def graphLoader: GraphLoader

  // --- RUNTIME ---

  private var graph: Graph = _
  private var session: Session = _

  override def open(): Unit = {
    graph = graphLoader.load()
    try {
      session = new Session(graph)
    }
    catch {
      case e: RuntimeException =>
        graph.close()
        throw e
    }
  }

  override def close(): Unit = {
    if(session != null) {
      session.close()
      session = null
    }
    if(graph != null) {
      graph.close()
      graph = null
    }
  }

  override def run[IN,OUT](input: IN)(implicit method: Signature[Self, IN, OUT]): OUT = {
    checkState(session != null)
    val context = new Model.RunContext {
      override def graph: Graph = that.graph
      override def session: Session = that.session
    }
    method.run(that, context, input)
  }
}
