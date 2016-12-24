package org.apache.flink.contrib.tensorflow.streaming;

import org.apache.flink.annotation.PublicEvolving;
import org.apache.flink.streaming.api.datastream.DataStream;

/**
 * Utility class for TensorFlow-based operations on {@link DataStream}.
 */
@PublicEvolving
public class TF {
	/**
	 * Visit a TensorFlow graph.
	 *
	 * @param input DataStream containing the input events
	 * @param <T>   Type of the input elements
	 * @return Resulting TF stream.
	 */
	public static <T> TensorFlowStream<T> flow(DataStream<T> input) {
		return new TensorFlowStream<>(input);
	}
}
