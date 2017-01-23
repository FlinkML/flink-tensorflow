package org.apache.flink.contrib.tensorflow.models.generic;

import org.tensorflow.Graph;

/**
 * Abstract loader to inspect and load a graph.
 */
public interface GraphLoader {

	/**
	 * Load the graph.
	 */
	Graph load();
}
