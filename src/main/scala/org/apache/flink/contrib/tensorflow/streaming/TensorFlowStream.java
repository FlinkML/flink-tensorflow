package org.apache.flink.contrib.tensorflow.streaming;

import org.apache.flink.annotation.PublicEvolving;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.typeutils.TypeExtractor;
import org.apache.flink.contrib.tensorflow.streaming.functions.GraphFunction;
import org.apache.flink.contrib.tensorflow.streaming.operator.KeyedGraphOperator;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.tensorflow.framework.GraphDef;
import scala.NotImplementedError;

import static org.apache.flink.util.Preconditions.checkNotNull;
import static org.apache.flink.util.Preconditions.checkState;

/**
 * Stream abstraction for a TensorFlow graph.
 *
 * @param <T> The type of the elements in this stream.
 */
@PublicEvolving
public class TensorFlowStream<T> {

	// underlying data stream
	private final DataStream<T> inputStream;

	private GraphDef graphDef;

	TensorFlowStream(final DataStream<T> inputStream) {
		this.inputStream = inputStream;
	}

	/**
	 * Returns the {@link StreamExecutionEnvironment} that was used to create this
	 * {@link TensorFlowStream}.
	 */
	public StreamExecutionEnvironment getExecutionEnvironment() {
		return inputStream.getExecutionEnvironment();
	}

	public TensorFlowStream<T> withGraphDef(GraphDef graphDef) {
		this.graphDef = graphDef;
		return this;
	}

	public <R> DataStream<R> process(final GraphFunction<T, R> graphFunction) {
		// we have to extract the output type from the provided pattern selection function manually
		// because the TypeExtractor cannot do that if the method is wrapped in a MapFunction

		TypeInformation<R> returnType = TypeExtractor.getUnaryOperatorReturnType(
			graphFunction,
			GraphFunction.class,
			1,
			-1,
			inputStream.getType(),
			null,
			false);

		return process(graphFunction, returnType);
	}

	public <R> DataStream<R> process(final GraphFunction<T, R> graphFunction, TypeInformation<R> outTypeInfo) {
		checkNotNull(graphFunction);
		checkNotNull(outTypeInfo);
		checkState(graphDef != null, "graphDef must be provided");

		final GraphFunction<T, R> cleanedGraphFunction = getExecutionEnvironment().clean(graphFunction);

		SingleOutputStreamOperator<R> result;
		if (inputStream instanceof KeyedStream) {
			throw new NotImplementedError("keyed inputStream");
		} else {
			KeyedStream<T, Byte> keyedStream = inputStream.keyBy(new NullByteKeySelector<T>());

			KeyedGraphOperator<Byte, T, R> operator = new KeyedGraphOperator<>(cleanedGraphFunction, graphDef);
			result = keyedStream
				.transform("Process", outTypeInfo, operator)
				.setParallelism(1);
		}

		return result;
	}

	/**
	 * Used as dummy KeySelector to allow using GlobalGraphOperator for non-keyed streams.
	 *
	 * @param <T>
	 */
	private static class NullByteKeySelector<T> implements KeySelector<T, Byte> {
		private static final long serialVersionUID = 1L;

		@Override
		public Byte getKey(T value) throws Exception {
			return 0;
		}
	}
}
