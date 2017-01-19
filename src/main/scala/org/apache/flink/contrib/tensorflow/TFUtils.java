package org.apache.flink.contrib.tensorflow;

import org.tensorflow.DataType;

/**
 */
public class TFUtils {

	public static int getValue(DataType dataType) {
		switch(dataType) {
			case FLOAT: return 1;
			case DOUBLE: return 2;
			case INT32: return 3;
			case STRING: return 7;
			case INT64: return 9;
			case BOOL: return 10;
			default: throw new IllegalArgumentException("dataType");
		}
	}

	public static DataType getDataType(int value) {
		switch(value) {
			case 1: return DataType.FLOAT;
			case 2: return DataType.DOUBLE;
			case 3: return DataType.INT32;
			case 7: return DataType.STRING;
			case 9: return DataType.INT64;
			case 10: return DataType.BOOL;
			default: throw new IllegalArgumentException("value");
		}
	}

	public static long[] squeeze(long[] shape) {
		int remaining = 0;
		for(int i = 0; i < shape.length; i++) {
			if(shape[i] != 1) remaining++;
		}
		long[] squeezed = new long[remaining];
		int j = 0;
		for(int i = 0; i < shape.length; i++) {
			if(shape[i] != 1) squeezed[j++] = shape[i];
		}
		return squeezed;
	}
}
