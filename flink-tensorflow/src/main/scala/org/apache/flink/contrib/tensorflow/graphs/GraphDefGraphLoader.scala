package org.apache.flink.contrib.tensorflow.graphs

import org.tensorflow.Graph
import org.tensorflow.contrib.scala.Graphs
import org.tensorflow.framework.GraphDef

/**
  * A graph loader based on a stored [[GraphDef]].
  *
  * @param graphDef the graph definition.
  */
@SerialVersionUID(1L)
class GraphDefGraphLoader(graphDef: GraphDef, prefix: String = "")
  extends GraphLoader with Serializable {
  override def load(): Graph = Graphs.load(graphDef)
}
