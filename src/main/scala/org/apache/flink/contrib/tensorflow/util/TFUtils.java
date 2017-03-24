package org.apache.flink.contrib.tensorflow.util;

import org.apache.flink.api.java.tuple.*;
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

	public static Tuple shapeOf(long[] s) {
		switch(s.length) {
			case 0:  return Tuple0.INSTANCE;
			case 1:  return new Tuple1<>(s[0]);
			case 2:  return  new Tuple2<>(s[0], s[1]);
			case 3:  return  new Tuple3<>(s[0], s[1], s[2]);
			case 4:  return  new Tuple4<>(s[0], s[1], s[2], s[3]);
			case 5:  return  new Tuple5<>(s[0], s[1], s[2], s[3], s[4]);
			case 6:  return  new Tuple6<>(s[0], s[1], s[2], s[3], s[4], s[5]);
			case 7:  return  new Tuple7<>(s[0], s[1], s[2], s[3], s[4], s[5], s[6]);
			case 8:  return  new Tuple8<>(s[0], s[1], s[2], s[3], s[4], s[5], s[6], s[7]);
			case 9:  return  new Tuple9<>(s[0], s[1], s[2], s[3], s[4], s[5], s[6], s[7], s[8]);
			case 10: return new Tuple10<>(s[0], s[1], s[2], s[3], s[4], s[5], s[6], s[7], s[8], s[9]);
			case 11: return new Tuple11<>(s[0], s[1], s[2], s[3], s[4], s[5], s[6], s[7], s[8], s[9], s[10]);
			case 12: return new Tuple12<>(s[0], s[1], s[2], s[3], s[4], s[5], s[6], s[7], s[8], s[9], s[10], s[11]);
			case 13: return new Tuple13<>(s[0], s[1], s[2], s[3], s[4], s[5], s[6], s[7], s[8], s[9], s[10], s[11], s[12]);
			case 14: return new Tuple14<>(s[0], s[1], s[2], s[3], s[4], s[5], s[6], s[7], s[8], s[9], s[10], s[11], s[12], s[13]);
			case 15: return new Tuple15<>(s[0], s[1], s[2], s[3], s[4], s[5], s[6], s[7], s[8], s[9], s[10], s[11], s[12], s[13], s[14]);
			case 16: return new Tuple16<>(s[0], s[1], s[2], s[3], s[4], s[5], s[6], s[7], s[8], s[9], s[10], s[11], s[12], s[13], s[14], s[15]);
			case 17: return new Tuple17<>(s[0], s[1], s[2], s[3], s[4], s[5], s[6], s[7], s[8], s[9], s[10], s[11], s[12], s[13], s[14], s[15], s[16]);
			case 18: return new Tuple18<>(s[0], s[1], s[2], s[3], s[4], s[5], s[6], s[7], s[8], s[9], s[10], s[11], s[12], s[13], s[14], s[15], s[16], s[17]);
			case 19: return new Tuple19<>(s[0], s[1], s[2], s[3], s[4], s[5], s[6], s[7], s[8], s[9], s[10], s[11], s[12], s[13], s[14], s[15], s[16], s[17], s[18]);
			case 20: return new Tuple20<>(s[0], s[1], s[2], s[3], s[4], s[5], s[6], s[7], s[8], s[9], s[10], s[11], s[12], s[13], s[14], s[15], s[16], s[17], s[18], s[19]);
			case 21: return new Tuple21<>(s[0], s[1], s[2], s[3], s[4], s[5], s[6], s[7], s[8], s[9], s[10], s[11], s[12], s[13], s[14], s[15], s[16], s[17], s[18], s[19], s[20]);
			case 22: return new Tuple22<>(s[0], s[1], s[2], s[3], s[4], s[5], s[6], s[7], s[8], s[9], s[10], s[11], s[12], s[13], s[14], s[15], s[16], s[17], s[18], s[19], s[20], s[21]);
			case 23: return new Tuple23<>(s[0], s[1], s[2], s[3], s[4], s[5], s[6], s[7], s[8], s[9], s[10], s[11], s[12], s[13], s[14], s[15], s[16], s[17], s[18], s[19], s[20], s[21], s[22]);
			case 24: return new Tuple24<>(s[0], s[1], s[2], s[3], s[4], s[5], s[6], s[7], s[8], s[9], s[10], s[11], s[12], s[13], s[14], s[15], s[16], s[17], s[18], s[19], s[20], s[21], s[22], s[23]);
			case 25: return new Tuple25<>(s[0], s[1], s[2], s[3], s[4], s[5], s[6], s[7], s[8], s[9], s[10], s[11], s[12], s[13], s[14], s[15], s[16], s[17], s[18], s[19], s[20], s[21], s[22], s[23], s[24]);
			default: throw new UnsupportedOperationException("unable to create a shape with rank > 25");
		}
	}
}
