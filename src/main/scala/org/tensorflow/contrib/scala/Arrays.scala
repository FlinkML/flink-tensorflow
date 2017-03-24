package org.tensorflow.contrib.scala

import java.nio.{DoubleBuffer, FloatBuffer, IntBuffer, LongBuffer}

import org.tensorflow.contrib.scala._
import com.twitter.bijection.Bijection
import org.apache.flink.api.java.tuple.Tuple
import org.tensorflow.Tensor
import org.tensorflow.contrib.scala.Rank.`1D`

/**
  * Support for Scala arrays.
  */
object Arrays {

  /**
    * Convert a [[Array]] of [[Int]]s to a 1-D [[Tensor]].
    */
  implicit def intArray2Tensor: Bijection[Array[Int],TypedTensor[`1D`,Int]] =
    Bijection.build[Array[Int], TypedTensor[`1D`,Int]] { arry =>
      Tensor.create(arry).taggedWith[`1D`,Int]
    } { t =>
      val buf = IntBuffer.allocate(t.numElements())
      t.writeTo(buf)
      buf.array()
    }

  /**
    * Convert a [[Array]] of [[Long]]s to a 1-D [[Tensor]].
    */
  implicit def longArray2Tensor[R <: Tuple]: Bijection[Array[Long],TypedTensor[`1D`,Long]] =
    Bijection.build[Array[Long], TypedTensor[`1D`,Long]] { arry =>
      Tensor.create(arry).taggedWith[`1D`,Long]
    } { t =>
      val buf = LongBuffer.allocate(t.numElements())
      t.writeTo(buf)
      buf.array()
    }

  /**
    * Convert a [[Array]] of [[Float]]s to a 1-D [[Tensor]].
    */
  implicit def floatArray2Tensor[R <: Tuple]: Bijection[Array[Float],TypedTensor[`1D`,Float]] =
    Bijection.build[Array[Float], TypedTensor[`1D`,Float]] { arry =>
      Tensor.create(arry).taggedWith[`1D`,Float]
    } { t =>
      val buf = FloatBuffer.allocate(t.numElements())
      t.writeTo(buf)
      buf.array()
    }

  /**
    * Convert a [[Array]] of [[Double]]s to a 1-D [[Tensor]].
    */
  implicit def doubleArray2Tensor[R <: Tuple]: Bijection[Array[Double],TypedTensor[`1D`,Double]] =
    Bijection.build[Array[Double], TypedTensor[`1D`,Double]] { arry =>
      Tensor.create(arry).taggedWith[`1D`,Double]
    } { t =>
      val buf = DoubleBuffer.allocate(t.numElements())
      t.writeTo(buf)
      buf.array()
    }
}
