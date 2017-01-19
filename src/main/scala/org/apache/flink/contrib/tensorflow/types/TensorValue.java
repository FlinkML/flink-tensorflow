/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.flink.contrib.tensorflow.types;

import org.apache.flink.annotation.Public;
import org.apache.flink.contrib.tensorflow.TFUtils;
import org.apache.flink.core.io.VersionMismatchException;
import org.apache.flink.core.memory.DataInputView;
import org.apache.flink.core.memory.DataOutputView;
import org.apache.flink.types.CopyableValue;
import org.tensorflow.DataType;
import org.tensorflow.Tensor;
import org.tensorflow.framework.TensorShapeProto;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.apache.flink.util.Preconditions.checkArgument;
import static org.apache.flink.util.Preconditions.checkNotNull;

/**
 * Mutable Tensor data type.
 * <p>
 * The mutability allows for reuse of the object inside the user code, also across invocations. Reusing a TensorValue object
 * helps to increase the performance, as tensor objects are heavy-weight objects when created and destroyed en masse.
 *
 * <p>The value arrays contained in this class are considered immutable, to facilitate efficient copies.
 * @see org.tensorflow.Tensor
 */
@Public
public final class TensorValue implements CopyableValue<TensorValue>
{
	private static final long serialVersionUID = 1L;

	DataType dataType;
	TensorShapeProto shape;
	transient ByteBuffer buffer;

	// --------------------------------------------------------------------------------------------
	//                                      Constructors
	// --------------------------------------------------------------------------------------------

	/**
	 * Required nullary constructor for instantiation by serialization logic.
	 */
	public TensorValue() {
	}

	/**
	 * Creates a tensor value with the given shape and buffer.
	 *
	 * Note: the tensor value assumes ownership of the buffer.
	 *
	 * @param buffer a buffer with native byte order.
	 */
	TensorValue(DataType dataType, TensorShapeProto shape, ByteBuffer buffer) {
		this.dataType = dataType;
		this.shape = checkNotNull(shape);
		checkArgument(buffer.order() == ByteOrder.nativeOrder(), "buffer has non-native byte order");
		this.buffer = checkNotNull(buffer);
	}

	/**
	 * Initializes this TensorValue to a copy of the given TensorValue.
	 *
	 * @param value The initial value.
	 */
	public TensorValue(TensorValue value) {
		value.copyTo(this);
	}

	// --------------------------------------------------------------------------------------------
	//                                Getters and Setters
	// --------------------------------------------------------------------------------------------

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(64);
		sb.append('†');
		sb.append(dataType);
		if(shape != null && shape.getDimCount() > 0) {
			sb.append('[');
			for(int i = 0; i < shape.getDimCount(); i++) {
				if(i > 0) sb.append(',');
				long size = shape.getDim(i).getSize();
				if(size > 0) sb.append(size);
				else sb.append('?');
			}
			sb.append(']');
		}
		return sb.toString();
	}

	// --------------------------------------------------------------------------------------------
	//                                    Tensor Methods
	// --------------------------------------------------------------------------------------------

	/**
	 * Returns the number of dimensions (sometimes referred to as <a
	 * href="https://www.tensorflow.org/resources/dims_types.html#rank">rank</a>) of the Tensor.
	 *
	 * <p>Will be 0 for a scalar, 1 for a vector, 2 for a matrix, 3 for a 3-dimensional tensor etc.
	 */
	public int numDimensions() {
		return this.shape.getDimCount();
	}

	/**
	 * Returns the <a href="https://www.tensorflow.org/resources/dims_types.html#shape">shape</a> of
	 * the Tensor, i.e., the sizes (and names) of each dimension.
	 */
	public TensorShapeProto shape() {
		return this.shape;
	}

	/**
	 * Creates a tensor based on this value.
	 *
	 * @return a tensor with a reference count of one.
	 */
	public Tensor toTensor() {
		Tensor t = Tensor.create(dataType, convertShape(shape), buffer);
		return t;
	}

	// --------------------------------------------------------------------------------------------
	//                            Serialization / De-Serialization
	// --------------------------------------------------------------------------------------------

	private static final byte VERSION_1 = 0x1;

	@Override
	public int getBinaryLength() {
		return 1 +
			(Integer.SIZE / Byte.SIZE) + // datatype
			(Integer.SIZE / Byte.SIZE) + shape.getSerializedSize() + // shape
			(Integer.SIZE / Byte.SIZE) + buffer.limit(); // data
	}

	@Override
	public void read(final DataInputView in) throws IOException {
		byte version = in.readByte();
		if(version != VERSION_1) {
			throw new VersionMismatchException("incompatible tensor value");
		}
		int dtype = in.readInt();
		this.dataType = TFUtils.getDataType(dtype);
		byte[] shapeData = new byte[in.readInt()];
		in.readFully(shapeData);
		this.shape = TensorShapeProto.parseFrom(shapeData);
		byte[] data = new byte[in.readInt()];
		in.readFully(data);
		this.buffer = ByteBuffer.wrap(data).order(ByteOrder.nativeOrder());
	}

	@Override
	public void write(final DataOutputView out) throws IOException {
		out.writeByte(VERSION_1);
		int dtype = TFUtils.getValue(this.dataType);
		out.writeInt(dtype);
		byte[] shapeData = this.shape.toByteArray();
		out.writeInt(shapeData.length);
		out.write(shapeData);
		ByteBuffer src = buffer.duplicate();
		src.rewind();
		out.writeInt(src.remaining());
		if(src.hasArray()) {
			out.write(src.array(), src.arrayOffset(), src.remaining());
		}
		else {
			byte[] data = new byte[src.remaining()];
			src.get(data);
			out.write(data);
		}
	}

	private static void copyInternal(DataInputView in, DataOutputView target) throws IOException {
		byte version = in.readByte();
		target.writeByte(version);
		int dtype = in.readInt();
		target.writeInt(dtype);
		int shapeLength = in.readInt();
		target.write(in, shapeLength);
		int dataLength = in.readInt();
		target.write(in, dataLength);
	}

	private void writeObject(java.io.ObjectOutputStream out) throws IOException {
		throw new UnsupportedOperationException("writeObject");
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
		throw new UnsupportedOperationException("readObject");
	}

	// --------------------------------------------------------------------------------------------
	//                                      CopyableValue
	// --------------------------------------------------------------------------------------------

	@Override
	public void copyTo(TensorValue target) {
		target.dataType = this.dataType;
		target.shape = this.shape;
		target.buffer = this.buffer.duplicate();
	}

	@Override
	public TensorValue copy() {
		return new TensorValue(this);
	}

	@Override
	public void copy(DataInputView in, DataOutputView target) throws IOException {
		copyInternal(in, target);
	}

	// --------------------------------------------------------------------------------------------
	//                                      Utilities
	// --------------------------------------------------------------------------------------------

	static TensorShapeProto convertShape(long[] shape) {
		TensorShapeProto.Builder b = TensorShapeProto.newBuilder();
		for(int i = 0; i < shape.length; i++) {
			b.addDim(TensorShapeProto.Dim.newBuilder().setSize(shape[i]));
		}
		return b.build();
	}

	static long[] convertShape(TensorShapeProto shape) {
		long[] b = new long[shape.getDimCount()];
		for(int i = 0; i < b.length; i++) {
			TensorShapeProto.Dim dim = shape.getDim(i);
			b[i] = dim.getSize();
		}
		return b;
	}

	// --------------------------------------------------------------------------------------------
	//                          Factory methods
	// --------------------------------------------------------------------------------------------

	/**
	 * Construct a {@link TensorValue} from a {@link Tensor}.
	 *
	 * <p>This method does not take ownership of the tensor.
	 */
	public static TensorValue fromTensor(Tensor t) {
		try {
			DataType dataType = t.dataType();
			TensorShapeProto shape = convertShape(t.shape());
			ByteBuffer buffer = ByteBuffer.allocate(t.byteSize()).order(ByteOrder.nativeOrder());
			t.writeTo(buffer);
			buffer.rewind();
			return new TensorValue(dataType, shape, buffer);
		}
		finally {
//			t.unref();
		}
	}
}
