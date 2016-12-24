package org.apache.flink.contrib.tensorflow.streaming.operator;

import org.apache.flink.contrib.tensorflow.streaming.functions.GraphFunction;
import org.apache.flink.ml.math.Vector;
import org.apache.flink.streaming.util.OneInputStreamOperatorTestHarness;
import org.apache.flink.util.Collector;
import org.junit.Test;
import org.tensorflow.framework.GraphDef;

/**
 */
public class AbstractGraphOperatorTest {

	static class TestAllGraphOperator<IN,OUT> extends AbstractGraphOperator<Byte,IN,OUT>  {
		public TestAllGraphOperator(GraphFunction<IN, OUT> userFunction) {
			super(userFunction, GraphDef.newBuilder().build());
		}
	}

	@Test
	public void testOpenClose() throws Exception {

		OneInputStreamOperatorTestHarness<Vector,Vector> harness = new OneInputStreamOperatorTestHarness<>(
			new TestAllGraphOperator<Vector,Vector>(new GraphFunction<Vector, Vector>() {
				@Override
				public void processElement(Vector value, Context ctx, Collector<Vector> out) throws Exception {

				}

				@Override
				public void onTimer(long timestamp, OnTimerContext ctx, Collector<Vector> out) throws Exception {

				}
			}));

		harness.open();
		try {

		}
		finally {
			harness.close();
		}
	}
}
