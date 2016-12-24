package org.apache.flink.contrib.tensorflow.streaming.functions;

import org.apache.flink.annotation.PublicEvolving;
import org.apache.flink.api.common.functions.Function;
import org.apache.flink.streaming.api.TimeDomain;
import org.apache.flink.streaming.api.TimerService;
import org.apache.flink.util.Collector;
import org.tensorflow.Graph;
import org.tensorflow.Session;

/**
 * A function that processes elements of a stream over a TensorFlow graph.
 *
 * @param <I> Type of the input elements.
 * @param <O> Type of the output elements.
 */
@PublicEvolving
public interface GraphFunction<I, O> extends Function {
	/**
	 * Process one element from the input stream.
	 * <p>
	 * <p>This function can output zero or more elements using the {@link Collector} parameter
	 * and also update internal state or set timers using the {@link Context} parameter.
	 *
	 * @param value The input value.
	 * @param ctx   A {@link Context} that allows querying the timestamp of the element and getting
	 *              a {@link TimerService} for registering timers and querying the time. The
	 *              context is only valid during the invocation of this method, do not store it.
	 * @param out   The collector for returning result values.
	 * @throws Exception This method may throw exceptions. Throwing an exception will cause the operation
	 *                   to fail and may trigger recovery.
	 */
	void processElement(I value, Context ctx, Collector<O> out) throws Exception;

	/**
	 * Called when a timer set using {@link TimerService} fires.
	 *
	 * @param timestamp The timestamp of the firing timer.
	 * @param ctx       An {@link OnTimerContext} that allows querying the timestamp of the firing timer,
	 *                  querying the {@link TimeDomain} of the firing timer and getting a
	 *                  {@link TimerService} for registering timers and querying the time.
	 *                  The context is only valid during the invocation of this method, do not store it.
	 * @param out       The collector for returning result values.
	 * @throws Exception This method may throw exceptions. Throwing an exception will cause the operation
	 *                   to fail and may trigger recovery.
	 */
	void onTimer(long timestamp, OnTimerContext ctx, Collector<O> out) throws Exception;

	/**
	 * Information available in an invocation of {@link #processElement(Object, Context, Collector)}
	 * or {@link #onTimer(long, OnTimerContext, Collector)}.
	 */
	interface Context {

		/**
		 * Timestamp of the element currently being processed or timestamp of a firing timer.
		 * <p>
		 * <p>This might be {@code null}, for example if the time characteristic of your program
		 * is set to {@link org.apache.flink.streaming.api.TimeCharacteristic#ProcessingTime}.
		 */
		Long timestamp();

		/**
		 * A {@link TimerService} for querying time and registering timers.
		 */
		TimerService timerService();

		/**
		 * The TensorFlow {@link Graph} to apply the input to.
		 */
		Graph graph();

		/**
		 * The TensorFlow {@link Session} associated with the operator.
		 */
		Session session();
	}

	/**
	 * Information available in an invocation of {@link #onTimer(long, OnTimerContext, Collector)}.
	 */
	interface OnTimerContext extends Context {
		/**
		 * The {@link TimeDomain} of the firing timer.
		 */
		TimeDomain timeDomain();
	}
}
