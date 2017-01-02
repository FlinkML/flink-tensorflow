package org.apache.flink.contrib.tensorflow.streaming.functions;

import org.apache.flink.api.common.functions.FoldFunction;
import org.apache.flink.api.common.functions.RichFoldFunction;
import org.apache.flink.contrib.tensorflow.common.TensorValue;

/**
 * A {@link FoldFunction} for folding tensor values.
 */
public class TensorFoldFunction extends RichFoldFunction<TensorValue, TensorValue> {
	@Override
	public TensorValue fold(TensorValue accumulator, TensorValue value) throws Exception {
		throw new UnsupportedOperationException("TensorFoldFunction::fold");
	}
}
