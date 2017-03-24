package org.tensorflow.contrib.scala

import com.twitter.bijection.Bijection
import org.tensorflow.contrib.scala.Rank._
import org.tensorflow.{DataType, Tensor}

/**
  * Support for TensorFlow byte strings.
  *
  * Byte strings are containers for variable-length data, typically protobuf messages
  * to be processed by a TF graph.   Tensors containing byte strings may be of any rank.
  *
  * It is useful to tag byte strings with information about the
  */
object ByteStrings {

  import scala.languageFeature.implicitConversions

  /**
    * Convert a [[ByteString]] to a 0-D [[TypedTensor]].
    */
  implicit def byteString2Tensor[T]: Bijection[ByteString[T], TypedTensor[`0D`, ByteString[T]]] =
    Bijection.build[ByteString[T], TypedTensor[`0D`, ByteString[T]]] { str =>
      Tensor.create(str: Array[Byte]).taggedWith[`0D`, ByteString[T]]
    } { t =>
      assert(t.dataType() == DataType.STRING)
      t.bytesValue().asInstanceOf[ByteString[T]]
    }

  /**
    * Implicit class providing convenience methods for byte arrays.
    */
  implicit class RichByteArray(array: Array[Byte]) {
    /**
      * View this byte array as a byte string representing an instance of [[T]].
      */
    def asByteString[T] = array.asInstanceOf[ByteString[T]]
  }
}
