package org.apache.flink.contrib.tensorflow.streaming.operator;

import org.apache.flink.contrib.tensorflow.streaming.functions.GraphFunction;
import org.tensorflow.framework.GraphDef;

/**
 * The {@link Graph} is the primary checkpointed state of the operator.
 */
public class KeyedGraphOperator<K, IN, OUT> extends AbstractGraphOperator<K, IN, OUT> {

	public KeyedGraphOperator(GraphFunction userFunction, GraphDef graphDef) {
		super(userFunction, graphDef);
	}
}
