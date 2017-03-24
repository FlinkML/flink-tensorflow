package org.apache.flink.contrib.tensorflow.graphs

import org.tensorflow.Graph

/**
 * Abstract loader to inspect and load a graph.
 */
trait GraphLoader {

	/**
	 * Load the graph.
	 */
	def load(): Graph
}
