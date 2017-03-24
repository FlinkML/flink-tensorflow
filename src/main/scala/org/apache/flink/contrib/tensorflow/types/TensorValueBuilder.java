package org.apache.flink.contrib.tensorflow.types;

import org.apache.flink.api.java.tuple.Tuple;
import org.tensorflow.DataType;

import java.nio.*;

/**
 * A builder for @{link TensorValue}.
 *
 * <p>Note: the supplied data is converted to a {@link ByteBuffer} as necessary.
 */
public class TensorValueBuilder<K extends Tuple,V> {

	private DataType dataType;
	private Tuple shape;
	private Buffer buffer;

	private TensorValueBuilder() {}

	public static TensorValueBuilder newBuilder() {
		return new TensorValueBuilder();
	}

	public TensorValueBuilder dataType(DataType dataType) {
		this.dataType = dataType;
		return this;
	}

	public TensorValueBuilder shape(long[] shape) {
		this.shape = TensorValue.convertShape(shape);
		return this;
	}

	public TensorValueBuilder shape(Tuple shape) {
		this.shape = shape;
		return this;
	}

	public TensorValueBuilder data(Buffer buffer) {
		this.buffer = buffer;
		return this;
	}

	public TensorValueBuilder data(int[] data) {
		this.buffer = IntBuffer.wrap(data);
		return this;
	}

	public TensorValueBuilder data(float[] data) {
		this.buffer = FloatBuffer.wrap(data);
		return this;
	}

	public TensorValueBuilder data(double[] data) {
		this.buffer = DoubleBuffer.wrap(data);
		return this;
	}

	public TensorValueBuilder data(long[] data) {
		this.buffer = LongBuffer.wrap(data);
		return this;
	}

	/**
	 * Build a {@link TensorValue}.
	 */
	public TensorValue build() {
		if(dataType == null) {
			throw new IllegalStateException("dataType is required");
		}
		if(shape == null) {
			throw new IllegalStateException("shape is required");
		}
		if(buffer == null) {
			throw new IllegalStateException("data is required");
		}

		final ByteBuffer data;
		if(buffer instanceof ByteBuffer && ((ByteBuffer) buffer).order() == ByteOrder.nativeOrder()) {
			data = (ByteBuffer) buffer;
		}
		else if(buffer instanceof IntBuffer) {
			data = ByteBuffer.allocate(Integer.SIZE / Byte.SIZE * buffer.remaining()).order(ByteOrder.nativeOrder());
			data.asIntBuffer().put((IntBuffer) buffer);
		}
		else if(buffer instanceof FloatBuffer) {
			data = ByteBuffer.allocate(Float.SIZE / Byte.SIZE * buffer.remaining()).order(ByteOrder.nativeOrder());
			data.asFloatBuffer().put((FloatBuffer) buffer);
		}
		else if(buffer instanceof DoubleBuffer) {
			data = ByteBuffer.allocate(Double.SIZE / Byte.SIZE * buffer.remaining()).order(ByteOrder.nativeOrder());
			data.asDoubleBuffer().put((DoubleBuffer) buffer);
		}
		else if(buffer instanceof LongBuffer) {
			data = ByteBuffer.allocate(Integer.SIZE / Byte.SIZE * buffer.remaining()).order(ByteOrder.nativeOrder());
			data.asLongBuffer().put((LongBuffer) buffer);
		}
		else {
			throw new IllegalStateException("unsupported data buffer");
		}
		buffer = null;

		return new TensorValue(dataType, shape, data);
	}
}
