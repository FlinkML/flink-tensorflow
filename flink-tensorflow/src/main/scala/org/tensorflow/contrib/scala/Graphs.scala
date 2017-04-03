package org.tensorflow.contrib.scala

import org.tensorflow.Graph
import org.tensorflow.framework.GraphDef
import resource.Resource

/**
  * Support for TensorFlow graphs.
  */
object Graphs {

  def load(graphDef: GraphDef): Graph = {
    val g = new Graph
    g.importGraphDef(graphDef.toByteArray())
    g
  }

  /**
    * Type class to treat a [[Graph]] instance as a managed resource.
    */
  implicit object graphResource extends Resource[Graph] {
    override def close(r: Graph): Unit = r.close()
    override def toString: String = "Resource[Graph]"
  }
}
