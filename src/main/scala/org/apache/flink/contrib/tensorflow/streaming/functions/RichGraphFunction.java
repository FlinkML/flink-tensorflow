package org.apache.flink.contrib.tensorflow.streaming.functions;

import org.apache.flink.annotation.PublicEvolving;
import org.apache.flink.api.common.functions.AbstractRichFunction;

/**
 * Rich variant of the {@link GraphFunction}. As a
 * {@link org.apache.flink.api.common.functions.RichFunction}, it gives access to the
 * {@link org.apache.flink.api.common.functions.RuntimeContext} and provides setup and teardown methods:
 * {@link org.apache.flink.api.common.functions.RichFunction#open(org.apache.flink.configuration.Configuration)}
 * and {@link org.apache.flink.api.common.functions.RichFunction#close()}.
 *
 * @param <I> Type of the input elements.
 * @param <O> Type of the returned elements.
 */
@PublicEvolving
public abstract class RichGraphFunction<I, O>
	extends AbstractRichFunction
	implements GraphFunction<I, O> {

	private static final long serialVersionUID = 1L;
}
