package org.apache.flink.contrib.tensorflow.types

import java.nio._

import com.google.protobuf.Message
import com.twitter.bijection.Inversion.attemptWhen
import com.twitter.bijection._
import com.twitter.bijection.protobuf.ProtobufCodec
import org.apache.flink.contrib.tensorflow.TFUtils
import org.tensorflow.{DataType, Tensor}

import scala.reflect.ClassTag

/**
  * Conversions to/from tensors.
  */
object TensorInjections
  extends Array2TensorInjections
  with TensorValue2TensorInjections
  with Message2TensorInjections {

  //  type InputLike[T] = Conversion[T,Tensor @@ Rep[T]]
  //  type OutputLike[T] = Conversion[Tensor @@ Rep[T],T]

//  type InputLike2[T] = Injection[T, Tensor @@ Rep[T]]
//  type OutputLike2[T] = Injection[Tensor @@ Rep[T], T]
//
//  type InputLike[T] = Injection[T, Tensor]
//  type OutputLike[T] = Injection[Tensor, T]
//
//  type TensorLike[T] = Injection[T, Tensor]
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
            val b = ByteBuffer.allocate(data.byteSize())
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
    * Convert a {@link Tensor} to a {@link TensorValue}.
    */
  implicit def tensor2TensorValue: Bijection[Tensor,TensorValue] =
    new AbstractBijection[Tensor,TensorValue] {
      def apply(t: Tensor) = TensorValue.fromTensor(t)
      override def invert(b: TensorValue): Tensor = b.toTensor
    }

  /**
    * Convert a {@link TensorValue} to a {@link Tensor}.
    */
  implicit def tensorValue2Tensor: Bijection[TensorValue,Tensor] =
    new AbstractBijection[TensorValue,Tensor] {
      def apply(t: TensorValue) = t.toTensor
      override def invert(b: Tensor): TensorValue = TensorValue.fromTensor(b)
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
