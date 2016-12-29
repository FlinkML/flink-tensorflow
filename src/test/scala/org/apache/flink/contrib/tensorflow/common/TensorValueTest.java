package org.apache.flink.contrib.tensorflow.common;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Ignore;
import org.junit.Test;
import org.tensorflow.DataType;
import org.tensorflow.Tensor;
import org.tensorflow.framework.TensorShapeProto;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.junit.Assert.*;

/**
 */
public class TensorValueTest {

	private Tensor testMatrix() {
		int[][] matrix = {{1, 2, 3}, {4, 5, 6}};
		return Tensor.create(matrix);
	}

	@Test
	public void createFromTensor() {
		int[][] matrix = {{1, 2, 3}, {4, 5, 6}};
		Tensor t = Tensor.create(matrix);
		TensorValue value = TensorValue.fromTensor(t);
		t.unref();

		assertEquals(2, value.shape().getDimCount());
		assertEquals(2, value.shape().getDim(0).getSize());
		assertEquals(3, value.shape().getDim(1).getSize());

		IntBuffer flattened = value.buffer.asIntBuffer();
		assertEquals(2, flattened.get(1));
		assertEquals(4, flattened.get(3));
	}

	@Test
	public void toTensor() {
		int[] expected = new int[]{1,2,3,4,5,6};
		{
			// build a test value
			TensorShapeProto shape = TensorShapeProto.newBuilder()
				.addDim(TensorShapeProto.Dim.newBuilder().setSize(2))
				.addDim(TensorShapeProto.Dim.newBuilder().setSize(3))
				.build();
			ByteBuffer buffer =
				ByteBuffer.allocate(expected.length * Integer.SIZE/Byte.SIZE).order(ByteOrder.nativeOrder());
			buffer.asIntBuffer().put(expected);
			TensorValue value = new TensorValue(DataType.INT32, shape, buffer);

			Tensor t = value.toTensor();
			assertEquals(1, t.refCount());
			assertEquals(2, t.numDimensions());

			int[] actual = new int[6];
			t.flatten(actual);
			assertArrayEquals(expected, actual);
		}
	}


}
