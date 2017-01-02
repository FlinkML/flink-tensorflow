package org.apache.flink.contrib.tensorflow.streaming.functions;

import org.apache.flink.annotation.Public;
import org.tensorflow.Graph;

/**
 * A graph initializer can be used to initialize the per-key graph state.
 *
 * <p>The initializer will be executed once per key when the
 * input is a {@link org.apache.flink.streaming.api.scala.KeyedStream}.
 *
 * @param <KEY> Type of key.
 */
@Public
public interface GraphInitializer<KEY> extends java.io.Serializable {

	/**
	 * Initializes the given graph for the given key.
	 */
	void initializeGraph(KEY key, Graph graph) throws Exception;

	class GraphInitializationException extends Exception {
		private static final long serialVersionUID = 1L;
		public GraphInitializationException(Exception cause) {
			super(cause);
		}
	}
}
