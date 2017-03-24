package org.apache.flink.contrib.tensorflow.types

import java.nio._

import com.google.protobuf.Message
import com.twitter.bijection.Inversion.attemptWhen
import com.twitter.bijection._
import com.twitter.bijection.protobuf.ProtobufCodec
import org.apache.flink.api.java.tuple.{Tuple => FlinkTuple}
import org.apache.flink.contrib.tensorflow.util.TFUtils
import org.tensorflow.{DataType, Tensor}

import scala.reflect.{ClassTag, classTag}
import scala.util.Try

/**
  * Conversions to/from tensors.
  */
object TensorInjections
  extends Array2TensorInjections
  with Scalar2TensorInjections
  with TensorValue2TensorInjections
  with Message2TensorInjections {

}

trait Message2TensorInjections {

  /**
    * Convert a {@code Array[Byte]} to a {@link Tensor} of type {@link DataType#STRING}.
    */
  private def bytes2Tensor: Injection[Array[Byte], Tensor] =
    new AbstractInjection[Array[Byte], Tensor] {
      def apply(arry: Array[Byte]) = Tensor.create(arry)

      override def invert(t: Tensor) =
        attemptWhen(t)(_.dataType() == DataType.STRING)(_.bytesValue())
    }

  /**
    * Convert a protobuf {@link Message} to a {@link Tensor} of type {@link DataType#STRING}.
    */
  implicit def message2Tensor[T <: Message : ClassTag]: Injection[T, Tensor] =
    ProtobufCodec[T] andThen bytes2Tensor

  /**
    * Convert a list of protobuf {@link Message}s to a {@link Tensor} of type {@link DataType#STRING}.
    */
  implicit def messages2Tensor[T <: Message : ClassTag]
  (implicit inj: Injection[T, Tensor]): Injection[List[T], Tensor] =
    new AbstractInjection[List[T], Tensor] {
      def apply(l: List[T]) = {
        // a hack to write a STRING tensor with numerous values
        val bb = ByteBuffer.allocate(10000).order(ByteOrder.nativeOrder())
        val dataStart = l.size * 8
        bb.position(dataStart)
        for (i <- l.indices) {
          val data = inj.apply(l(i))
          try {
            val b = ByteBuffer.allocate(data.numBytes())
            data.writeTo(b)
            b.flip().position(8)
            bb.putLong(i * 8, bb.position() - dataStart)
            bb.put(b)
          }
          finally {
            data.close()
          }
        }
        bb.flip()
        Tensor.create(DataType.STRING, Array(l.size.toLong), bb)
      }

      override def invert(t: Tensor) =
        attemptWhen(t)(_.dataType() == DataType.STRING) {
          ???
        }
    }
}

trait TensorValue2TensorInjections {

  /**
    * Convert a [[TensorValue]] of [[Float]] to a [[Tensor]].
    */
  implicit def floatTensorValue2Tensor[K <: FlinkTuple : ClassTag]: Injection[TensorValue[K,Float],Tensor] =
    new AbstractInjection[TensorValue[K,Float],Tensor] {
      private val k = classTag[K]
      def apply(t: TensorValue[K,Float]) = t.toTensor
      override def invert(t: Tensor): Try[TensorValue[K,Float]] = {
        val isK = k.runtimeClass == FlinkTuple.getTupleClass(t.shape().length)
        attemptWhen(t)(t => isK && t.dataType()==DataType.FLOAT) { t =>
          // TODO(eronwright) - use FloatBuffer
          TensorValue.fromTensor[K, Float](t)
        }
      }
    }

  /**
    * Convert a [[TensorValue]] of [[Double]] to a [[Tensor]].
    */
  implicit def doubleTensorValue2Tensor[K <: FlinkTuple : ClassTag]: Injection[TensorValue[K,Double],Tensor] =
    new AbstractInjection[TensorValue[K,Double],Tensor] {
      def apply(t: TensorValue[K,Double]) = t.toTensor
      override def invert(t: Tensor): Try[TensorValue[K,Double]] = {
        val isK = classTag[K].runtimeClass == FlinkTuple.getTupleClass(t.shape().length)
        attemptWhen(t)(t => isK && t.dataType()==DataType.DOUBLE) { t =>
          // TODO(eronwright) - use DoubleBuffer
          TensorValue.fromTensor[K, Double](t)
        }
      }
    }

  /**
    * Convert a [[TensorValue]] of [[Long]] to a [[Tensor]].
    */
  implicit def longTensorValue2Tensor[K <: FlinkTuple : ClassTag]: Injection[TensorValue[K,Long],Tensor] =
    new AbstractInjection[TensorValue[K,Long],Tensor] {
      def apply(t: TensorValue[K,Long]) = t.toTensor
      override def invert(t: Tensor): Try[TensorValue[K,Long]] = {
        val isK = classTag[K].runtimeClass == FlinkTuple.getTupleClass(t.shape().length)
        attemptWhen(t)(t => isK && t.dataType()==DataType.INT64) { t =>
          // TODO(eronwright) - use LongBuffer
          TensorValue.fromTensor[K, Long](t)
        }
      }
    }

  /**
    * Convert a [[TensorValue]] of [[Int]] to a [[Tensor]].
    */
  implicit def intTensorValue2Tensor[K <: FlinkTuple : ClassTag]: Injection[TensorValue[K,Int],Tensor] =
    new AbstractInjection[TensorValue[K,Int],Tensor] {
      def apply(t: TensorValue[K,Int]) = t.toTensor
      override def invert(t: Tensor): Try[TensorValue[K,Int]] = {
        val isK = classTag[K].runtimeClass == FlinkTuple.getTupleClass(t.shape().length)
        attemptWhen(t)(t => isK && t.dataType()==DataType.INT32) { t =>
          // TODO(eronwright) - use IntBuffer
          TensorValue.fromTensor[K, Int](t)
        }
      }
    }

  implicit def messageTensorValue2Tensor[K <: FlinkTuple : ClassTag, V <: Message]: Injection[TensorValue[K,V],Tensor] =
    new AbstractInjection[TensorValue[K,V],Tensor] {
      def apply(t: TensorValue[K,V]) = t.toTensor
      override def invert(t: Tensor): Try[TensorValue[K,V]] = {
        val isK = classTag[K].runtimeClass == FlinkTuple.getTupleClass(t.shape().length)
        attemptWhen(t)(t => isK && t.dataType()==DataType.STRING) { t =>
          TensorValue.fromTensor[K, V](t)
        }
      }
    }
}

trait Array2TensorInjections {
  /**
    * Embeds an array in a 1D Tensor.
    */
  implicit def floatArray2Tensor: Injection[Array[Float], Tensor] =
    new AbstractInjection[Array[Float], Tensor] {
      def apply(arry: Array[Float]) = Tensor.create(arry)
      override def invert(t: Tensor) =
        attemptWhen(t)(t => t.dataType() == DataType.FLOAT && TFUtils.squeeze(t.shape()).length <= 1) { t =>
          val buffer = FloatBuffer.allocate(t.numElements())
          t.writeTo(buffer)
          buffer.array()
        }
    }

  /**
    * Embeds an array in a 1D Tensor.
    */
  implicit def doubleArray2Tensor: Injection[Array[Double], Tensor] =
    new AbstractInjection[Array[Double], Tensor] {
      def apply(arry: Array[Double]) = Tensor.create(arry)
      override def invert(t: Tensor) =
        attemptWhen(t)(t => t.dataType() == DataType.DOUBLE && TFUtils.squeeze(t.shape()).length <= 1) { t =>
          val buffer = DoubleBuffer.allocate(t.numElements())
          t.writeTo(buffer)
          buffer.array()
        }
    }

  /**
    * Embeds an array in a 1D Tensor.
    */
  implicit def longArray2Tensor: Injection[Array[Long], Tensor] =
    new AbstractInjection[Array[Long], Tensor] {
      def apply(arry: Array[Long]) = Tensor.create(arry)
      override def invert(t: Tensor) =
        attemptWhen(t)(t => t.dataType() == DataType.INT64 && TFUtils.squeeze(t.shape()).length <= 1) { t =>
          val buffer = LongBuffer.allocate(t.numElements())
          t.writeTo(buffer)
          buffer.array()
        }
    }

  /**
    * Embeds an array in a 1D Tensor.
    */
  implicit def intArray2Tensor: Injection[Array[Int], Tensor] =
    new AbstractInjection[Array[Int], Tensor] {
      def apply(arry: Array[Int]) = Tensor.create(arry)
      override def invert(t: Tensor) =
        attemptWhen(t)(t => t.dataType() == DataType.INT32 && TFUtils.squeeze(t.shape()).length <= 1) { t =>
          val buffer = IntBuffer.allocate(t.numElements())
          t.writeTo(buffer)
          buffer.array()
        }
    }
}

trait Scalar2TensorInjections {
  /**
    * Embeds a [[String]] in a 0-D Tensor.
    */
  implicit def string2Tensor: Injection[String, Tensor] =
    new AbstractInjection[String, Tensor] {
      def apply(str: String) = Tensor.create(str.getBytes)
      override def invert(t: Tensor) =
        attemptWhen(t)(t => t.dataType() == DataType.STRING && t.shape().length == 0) { t =>
          new String(t.bytesValue())
        }
    }

  /**
    * Embeds a [[Float]] in a 0-D Tensor.
    */
  implicit def float2Tensor: Injection[Float, Tensor] =
    new AbstractInjection[Float, Tensor] {
      def apply(value: Float) = Tensor.create(value)
      override def invert(t: Tensor) =
        attemptWhen(t)(t => t.dataType() == DataType.FLOAT && t.shape().length == 0) { t =>
          t.floatValue()
        }
    }

  /**
    * Embeds a [[Double]] in a 0-D Tensor.
    */
  implicit def double2Tensor: Injection[Double, Tensor] =
    new AbstractInjection[Double, Tensor] {
      def apply(value: Double) = Tensor.create(value)
      override def invert(t: Tensor) =
        attemptWhen(t)(t => t.dataType() == DataType.DOUBLE && t.shape().length == 0) { t =>
          t.doubleValue()
        }
    }

  /**
    * Embeds a [[Long]] in a 0-D Tensor.
    */
  implicit def long2Tensor: Injection[Long, Tensor] =
    new AbstractInjection[Long, Tensor] {
      def apply(value: Long) = Tensor.create(value)
      override def invert(t: Tensor) =
        attemptWhen(t)(t => t.dataType() == DataType.INT64 && t.shape().length == 0) { t =>
          t.longValue()
        }
    }

  /**
    * Embeds an [[Int]] in a 0-D Tensor.
    */
  implicit def int2Tensor: Injection[Int, Tensor] =
    new AbstractInjection[Int, Tensor] {
      def apply(value: Int) = Tensor.create(value)
      override def invert(t: Tensor) =
        attemptWhen(t)(t => t.dataType() == DataType.INT32 && t.shape().length == 0) { t =>
          t.intValue()
        }
    }
}
