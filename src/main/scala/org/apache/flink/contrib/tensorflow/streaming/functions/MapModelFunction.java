package org.apache.flink.contrib.tensorflow.streaming.functions;

import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.contrib.tensorflow.models.Model;

/**
 * Map input elements to output elements using a model.
 */
public class MapModelFunction<IN, OUT> extends RichMapFunction<IN, OUT> {

	@Override
	public void open(Configuration parameters) throws Exception {
		super.open(parameters);

		// open the model and locate the model method by signature
	}

	@Override
	public void close() throws Exception {
		super.close();
	}

	@Override
	public OUT map(IN value) throws Exception {

		return null;

		// convert the input to tensor
		// invoke the model method
//		return method.invoke(value);
	}
}
