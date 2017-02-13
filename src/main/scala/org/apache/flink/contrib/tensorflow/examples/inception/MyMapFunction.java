package org.apache.flink.contrib.tensorflow.examples.inception;

import org.apache.flink.contrib.tensorflow.common.functions.AbstractMapFunction;
import org.apache.flink.contrib.tensorflow.ml.models.HalfPlusTwo;
import org.apache.flink.contrib.tensorflow.ml.signatures.RegressionSignature$;
import org.apache.flink.contrib.tensorflow.models.Model;

import org.tensorflow.example.Example;

import java.util.Collections;
import org.apache.flink.contrib.tensorflow.ml.signatures.RegressionSignature.RegressionOutputs;

/**
 * A sample function to test that the Scala API is usable from Java.
 */
@Deprecated
public class MyMapFunction extends AbstractMapFunction<Example,Float> {

	@Override
	public Float map(Example value) throws Exception {
		HalfPlusTwo model = (HalfPlusTwo) model();
//		RegressionOutputs outputs = (RegressionOutputs) model.regress(
//			scala.collection.JavaConversions.asScalaBuffer(Collections.singletonList(value)),
//			RegressionSignature$.MODULE$.<HalfPlusTwo>regressWithSeqOfExamples());
//
//		return outputs.output()[0];

		throw new UnsupportedOperationException("MyMapFunction");
	}

	@Override
	public Model<HalfPlusTwo> model() {
		return new HalfPlusTwo(null);
	}
}
