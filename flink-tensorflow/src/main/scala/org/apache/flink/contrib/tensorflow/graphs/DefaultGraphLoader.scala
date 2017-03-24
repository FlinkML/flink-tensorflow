package org.apache.flink.contrib.tensorflow.graphs

import org.apache.flink.contrib.tensorflow.util.GraphUtils
import org.apache.flink.core.fs.Path
import org.slf4j.{Logger, LoggerFactory}
import org.tensorflow.Graph

/**
  * The default graph loader.
  *
  * <p>Loads graphs from a distributed filesystem.
  *
  * @param graphPath the path to load from.
  */
class DefaultGraphLoader(graphPath: Path, prefix: String = "") extends GraphLoader {
  protected val LOG: Logger = LoggerFactory.getLogger(classOf[DefaultGraphLoader])

  override def load(): Graph = {
    val graph = GraphUtils.importFromPath(graphPath, prefix)
    LOG.info(s"loaded $graphPath")
    graph
  }
}
