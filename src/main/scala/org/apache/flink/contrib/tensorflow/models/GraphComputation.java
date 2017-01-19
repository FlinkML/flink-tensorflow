package org.apache.flink.contrib.tensorflow.models;

import org.apache.flink.annotation.Public;
import org.tensorflow.Session;
import org.tensorflow.Tensor;

import java.util.Map;

/**
 * The base interface for graph computations.
 */
@Public
public interface GraphComputation {

	/**
	 * Apply the computation to the graph.
	 *
	 * @param session the session containing the graph to apply to.
	 * @param inputs  the input tensors to the computation.   The caller is responsible for closing the tensors.
	 * @return a computation result containing output tensors.  The caller is responsible for closing the result.
	 */
	Result run(Session session, Map<String, Tensor> inputs);

	interface Result extends AutoCloseable {
		// TODO provide run metadata

//		/**
//		 * Takes the output tensor with the given key.
//		 *
//		 * <p>Take a tensor to prevent it from being closed when the overall result is closed.
//		 */
//		Tensor take(String key);

		/**
		 * A mutable map of all output tensors.
		 * <p>
		 * Remove tensors to prevent them being closed when the overall result is closed.
		 */
		Map<String, Tensor> outputs();
	}
}
