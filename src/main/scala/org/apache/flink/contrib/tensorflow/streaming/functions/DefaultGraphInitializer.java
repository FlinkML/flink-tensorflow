package org.apache.flink.contrib.tensorflow.streaming.functions;

import org.tensorflow.Graph;
import org.tensorflow.framework.GraphDef;

/**
 */
public abstract class DefaultGraphInitializer<KEY> implements GraphInitializer<KEY> {

	private final GraphDef graphDef;

	public DefaultGraphInitializer() {
		this.graphDef = buildGraph();
	}

	protected abstract GraphDef buildGraph();

	@Override
	public void initializeGraph(KEY key, Graph graph) throws Exception {
		try {
			graph.importGraphDef(graphDef.toByteArray());
		} catch (IllegalArgumentException ex) {
			throw new GraphInitializationException(ex);
		}
	}
}
