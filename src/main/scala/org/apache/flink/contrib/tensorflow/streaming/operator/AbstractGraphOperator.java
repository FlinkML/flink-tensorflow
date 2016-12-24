package org.apache.flink.contrib.tensorflow.streaming.operator;

import org.apache.flink.contrib.tensorflow.streaming.functions.GraphFunction;
import org.apache.flink.runtime.state.VoidNamespace;
import org.apache.flink.runtime.state.VoidNamespaceSerializer;
import org.apache.flink.streaming.api.SimpleTimerService;
import org.apache.flink.streaming.api.TimeDomain;
import org.apache.flink.streaming.api.TimerService;
import org.apache.flink.streaming.api.operators.*;
import org.apache.flink.streaming.runtime.streamrecord.StreamRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tensorflow.Graph;
import org.tensorflow.Session;
import org.tensorflow.framework.GraphDef;

import static org.apache.flink.util.Preconditions.checkNotNull;
import static org.apache.flink.util.Preconditions.checkState;

/**
 *
 */
public abstract class AbstractGraphOperator<K, IN, OUT>
	extends AbstractUdfStreamOperator<OUT, GraphFunction<IN, OUT>>
	implements OneInputStreamOperator<IN, OUT>, Triggerable<K, VoidNamespace> {

	protected static final Logger LOG = LoggerFactory.getLogger(AbstractGraphOperator.class);

	private static final long serialVersionUID = 1L;

	// ----------- configuration properties -------------

	private GraphDef graphDef;

	// ---------------- key/value state ------------------

	//
	// ---------------- runtime ------------------

	// the TF graph instance
	private transient Graph graph;

	// the TF session for the operator instance
	private transient Session session;

	// collector for user graph function
	private transient TimestampedCollector<OUT> collector;

	// timer service
	private transient TimerService timerService;

	private transient ContextImpl<IN> context;

	private transient OnTimerContextImpl onTimerContext;

	public AbstractGraphOperator(GraphFunction<IN, OUT> userFunction, GraphDef graphDef) {
		super(userFunction);
		chainingStrategy = ChainingStrategy.ALWAYS;
		this.graphDef = graphDef;
	}

	/**
	 * Open the graph operator.
	 */
	@Override
	public void open() throws Exception {
		super.open();

		graph = new Graph();
		try {
			graph.importGraphDef(graphDef.toByteArray());
		} catch (IllegalArgumentException ex) {
			throw new RuntimeException("unreadable graphdef", ex);
		}

		session = new Session(graph);

		collector = new TimestampedCollector<>(output);

		InternalTimerService<VoidNamespace> internalTimerService =
			getInternalTimerService("user-timers", VoidNamespaceSerializer.INSTANCE, this);

		this.timerService = new SimpleTimerService(internalTimerService);

		context = new ContextImpl<>(timerService, graph, session);
		onTimerContext = new OnTimerContextImpl(timerService, graph, session);

		LOG.debug("Opened");
	}

	@Override
	public void close() throws Exception {
		if (session != null) {
			session.close();
			session = null;
		}
		if (graph != null) {
			graph.close();
			graph = null;
		}
		super.close();
		LOG.debug("Closed");
	}

	@Override
	public void dispose() throws Exception {
		if (session != null) {
			session.close();
			session = null;
		}
		if (graph != null) {
			graph.close();
			graph = null;
		}
		super.dispose();
	}

	// ---------------- element processing ------------------

	@Override
	public void onEventTime(InternalTimer<K, VoidNamespace> timer) throws Exception {
		collector.setAbsoluteTimestamp(timer.getTimestamp());
		onTimerContext.init(TimeDomain.EVENT_TIME, timer);
		try {
			userFunction.onTimer(timer.getTimestamp(), onTimerContext, collector);
		} finally {
			onTimerContext.reset();
		}
	}

	@Override
	public void onProcessingTime(InternalTimer<K, VoidNamespace> timer) throws Exception {
		collector.setAbsoluteTimestamp(timer.getTimestamp());
		onTimerContext.init(TimeDomain.PROCESSING_TIME, timer);
		try {
			userFunction.onTimer(timer.getTimestamp(), onTimerContext, collector);
		} finally {
			onTimerContext.reset();
		}
	}

	@Override
	public void processElement(StreamRecord<IN> element) throws Exception {
		collector.setTimestamp(element);
		context.init(element);
		try {
			userFunction.processElement(element.getValue(), context, collector);
		} finally {
			context.reset();
		}
	}

	private static class ContextImpl<T> implements GraphFunction.Context {

		protected final TimerService timerService;

		protected final Graph graph;

		protected final Session session;

		private StreamRecord<T> element;

		ContextImpl(TimerService timerService, Graph graph, Session session) {
			this.timerService = checkNotNull(timerService);
			this.graph = checkNotNull(graph);
			this.session = checkNotNull(session);
		}

		void init(StreamRecord<T> element) {
			this.element = element;
		}

		void reset() {
			this.element = null;
		}

		@Override
		public Long timestamp() {
			checkState(element != null);

			if (element.hasTimestamp()) {
				return element.getTimestamp();
			} else {
				return null;
			}
		}

		@Override
		public TimerService timerService() {
			return timerService;
		}

		@Override
		public Graph graph() {
			return graph;
		}

		@Override
		public Session session() {
			return session;
		}
	}

	private static class OnTimerContextImpl implements GraphFunction.OnTimerContext {

		private final TimerService timerService;

		private final Graph graph;

		private final Session session;

		private TimeDomain timeDomain;

		private InternalTimer<?, VoidNamespace> timer;

		OnTimerContextImpl(TimerService timerService, Graph graph, Session session) {
			this.timerService = checkNotNull(timerService);
			this.graph = checkNotNull(graph);
			this.session = checkNotNull(session);
		}

		void init(TimeDomain timeDomain, InternalTimer<?, VoidNamespace> timer) {
			this.timeDomain = timeDomain;
			this.timer = timer;
		}

		void reset() {
			this.timeDomain = null;
			this.timer = null;
		}

		@Override
		public TimeDomain timeDomain() {
			checkState(timeDomain != null);
			return timeDomain;
		}

		@Override
		public Long timestamp() {
			checkState(timer != null);
			return timer.getTimestamp();
		}

		@Override
		public TimerService timerService() {
			return timerService;
		}

		@Override
		public Graph graph() {
			return graph;
		}

		@Override
		public Session session() {
			return session;
		}
	}
}
