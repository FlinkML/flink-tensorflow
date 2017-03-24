package org.apache.flink.contrib.tensorflow.types;

import org.apache.flink.api.java.tuple.Tuple1;
import org.apache.flink.api.java.tuple.Tuple2;
import org.junit.Test;
import org.tensorflow.DataType;
import org.tensorflow.Tensor;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * Tests for TensorValue.
 */
public class TensorValueTest {

	@Test
	public void createFromTensor() {
		int[][] matrix = {{1, 2, 3}, {4, 5, 6}};
		TensorValue<Tuple2<Long,Long>, Integer> value;
		try(Tensor t = Tensor.create(matrix)) {
			value = TensorValue.fromTensor(t);
		}

		assertEquals(2, value.shape().getArity());
		assertEquals(2, (long) value.shape().f0);
		assertEquals(3, (long) value.shape().f1);

		IntBuffer flattened = value.buffer.asIntBuffer();
		assertEquals(2, flattened.get(1));
		assertEquals(4, flattened.get(3));
	}

	@Test
	public void toTensor() {
		int[] expected = new int[]{1,2,3,4,5,6};
		{
			// build a test value
			Tuple1<Long> shape = Tuple1.of(6L);
			ByteBuffer buffer =
				ByteBuffer.allocate(expected.length * Integer.SIZE/Byte.SIZE).order(ByteOrder.nativeOrder());
			buffer.asIntBuffer().put(expected);
			TensorValue<Tuple1<Long>, Integer> value = new TensorValue<>(DataType.INT32, shape, buffer);

			try(Tensor t = value.toTensor()) {
				assertEquals(1, t.numDimensions());

				int[] actual = new int[6];
				t.copyTo(actual);
				assertArrayEquals(expected, actual);
			}
		}
	}


}
