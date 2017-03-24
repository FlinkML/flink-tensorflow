package org.tensorflow.contrib

import com.twitter.bijection.Bijection
import org.apache.flink.contrib.tensorflow.types.TensorValue
import com.twitter.bijection._
import org.tensorflow.Session.Run
import org.tensorflow.{Graph, Session, Tensor}
import resource._

package object scala {

  /**
    * The element type of a ByteString tensor.
    *
    * @tparam T the embedded type that the bytestring represents.
    */
  type ByteStr[T] = Array[Byte] @@ T

  /**
    * Tensor tagging support, to associate rank and datatype information with a tensor
    * in the type system, without requiring a wrapper class.
    *
    * Inspired by the @@-style tagging provided by scalaz/shapeless.
    */

  /**
    * A trait for tagging a tensor with additional type information.
    *
    * @tparam K the tensor rank
    * @tparam V the tensor value type
    */
  trait TensorTypeTag[K, V]

  /**
    * A tensor with additional type information conveyed as a tag.
    *
    * @tparam K the tensor rank
    * @tparam V the tensor value type
    */
  type TypedTensor[K, V] = Tensor with TensorTypeTag[K, V]

  /**
    * An implicit class for tagging a tensor with type information.
    *
    * @param t the tensor
    */
  implicit class TensorTagger(t: Tensor) {
    /**
      * Associate additional type information with the tensor.
      *
      * @tparam K the tensor rank
      * @tparam V the tensor value type
      * @return a tensor with rank and value type
      */
    def taggedWith[K, V]: TypedTensor[K, V] = t.asInstanceOf[TypedTensor[K, V]]

    /**
      * Associate additional type information with the tensor.
      *
      * @tparam T the tensor type
      * @return a tensor with rank and value type
      */
    def taggedAs[T <: TypedTensor[_, _]]: T = t.asInstanceOf[T]
  }
}
