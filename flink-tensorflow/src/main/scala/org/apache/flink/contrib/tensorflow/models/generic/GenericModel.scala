package org.apache.flink.contrib.tensorflow.models.generic

import org.apache.flink.contrib.tensorflow.graphs.GraphLoader
import org.apache.flink.contrib.tensorflow.models.RichModel
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

  @transient protected[this] var graph: Graph = _
  @transient protected[this] var session: Session = _

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
}
