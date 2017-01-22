package org.apache.flink.contrib.tensorflow.examples.inception;

import org.apache.flink.contrib.tensorflow.common.functions.AbstractMapFunction;
import org.apache.flink.contrib.tensorflow.ml.models.HalfPlusTwo;
import org.apache.flink.contrib.tensorflow.ml.signatures.RegressionSignature;
import org.apache.flink.contrib.tensorflow.models.Model;

import org.tensorflow.example.Example;

import java.util.Collections;

/**
 * A sample function to test that the Scala API is usable from Java.
 */
@Deprecated
public class MyMapFunction extends AbstractMapFunction<Example,Float> {

	@Override
	public Float map(Example value) throws Exception {
		HalfPlusTwo model = (HalfPlusTwo) model();
		RegressionSignature.RegressionOutputs outputs = (RegressionSignature.RegressionOutputs) model.run(
			scala.collection.JavaConversions.asScalaBuffer(Collections.singletonList(value)),
			model.regress());

		return outputs.output()[0];
	}

	@Override
	public Model<HalfPlusTwo> model() {
		return new HalfPlusTwo(null);
	}
}
