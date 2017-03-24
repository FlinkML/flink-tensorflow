package org.apache.flink.contrib

import com.twitter.bijection.Bijection
import org.apache.flink.api.java.tuple.{Tuple => FlinkTuple}
import org.apache.flink.contrib.tensorflow.types.TensorValue
import org.tensorflow.Tensor
import org.tensorflow.contrib.scala._

package object tensorflow {

  /**
    * Convert a [[TensorValue]] to a [[Tensor]].
    */
  implicit def tensorValueToTensor[K <: FlinkTuple,V]: Bijection[TensorValue[K,V], TypedTensor[K,V]] =
    Bijection.build[TensorValue[K,V], TypedTensor[K,V]] { value =>
      value.toTensor.taggedWith[K,V]
    } { t =>
      TensorValue.fromTensor[K,V](t)
    }

  /**
    * Enhances [[TypedTensor]] with Flink-specific functionality.
    */
  implicit class RichTensor[K <: FlinkTuple,V](t: TypedTensor[K,V]) {

    /**
      * Convert the tensor to a serializable tensor value.
      *
      * This method does not take ownership of the tensor.
      */
    def toValue: TensorValue[K,V] = TensorValue.fromTensor[K,V](t)
  }
}
