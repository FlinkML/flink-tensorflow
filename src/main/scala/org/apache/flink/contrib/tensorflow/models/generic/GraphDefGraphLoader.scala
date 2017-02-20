package org.apache.flink.contrib.tensorflow.models.generic

import org.apache.flink.contrib.tensorflow.examples.common.GraphBuilder
import org.apache.flink.contrib.tensorflow.util.GraphUtils
import org.apache.flink.core.fs.Path
import org.tensorflow.Graph
import org.tensorflow.framework.GraphDef

/**
  * A graph loader based on a stored [[GraphDef]].
  *
  * @param graphDef the graph definition.
  */
@SerialVersionUID(1L)
class GraphDefGraphLoader(graphDef: GraphDef, prefix: String = "")
  extends GraphLoader with Serializable {
  override def load(): Graph = {
    GraphBuilder.fromGraphDef(graphDef, prefix)
  }
}
