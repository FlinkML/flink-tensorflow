package org.apache.flink.contrib.tensorflow.common;

import org.apache.flink.api.common.typeutils.TypeSerializer;
import org.apache.flink.core.memory.DataInputView;
import org.apache.flink.core.memory.DataOutputView;
import org.tensorflow.Graph;

import java.io.IOException;

/**
 */
@Deprecated
public class GraphSerializer extends TypeSerializer<Graph> {

	public static final GraphSerializer INSTANCE = new GraphSerializer();

	@Override
	public boolean isImmutableType() {
		return false;
	}

	@Override
	public TypeSerializer<Graph> duplicate() {
		return new GraphSerializer();
	}

	@Override
	public Graph createInstance() {
		return new Graph();
	}

	@Override
	public Graph copy(Graph from) {
		// TODO

		throw new UnsupportedOperationException("GraphSerializer::copy(from)");
	}

	@Override
	public Graph copy(Graph from, Graph reuse) {
		// TODO

		throw new UnsupportedOperationException("GraphSerializer::copy(from, reuse)");
	}

	@Override
	public int getLength() {
		return -1;
	}

	@Override
	public void serialize(Graph record, DataOutputView target) throws IOException {
		// TODO

	}

	@Override
	public Graph deserialize(DataInputView source) throws IOException {
		// TODO
		return new Graph();
	}

	@Override
	public Graph deserialize(Graph reuse, DataInputView source) throws IOException {
		// TODO
		return new Graph();
	}

	@Override
	public void copy(DataInputView source, DataOutputView target) throws IOException {
		throw new UnsupportedOperationException("GraphSerializer::copy(source, target)");
	}

	@Override
	public boolean equals(Object obj) {
		if(canEqual(obj)) {
			GraphSerializer other = (GraphSerializer) obj;
			// TODO
			return true;
		}
		return false;
	}

	@Override
	public boolean canEqual(Object obj) {
		return obj instanceof GraphSerializer;
	}

	@Override
	public int hashCode() {
		// TODO
		return 0;
	}
}
