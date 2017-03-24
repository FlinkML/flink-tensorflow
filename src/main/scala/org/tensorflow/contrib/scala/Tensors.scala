package org.tensorflow.contrib.scala

import java.nio.FloatBuffer

import com.twitter.bijection._
import org.apache.flink.api.java.tuple.Tuple
import org.apache.flink.contrib.tensorflow.types.TensorValue
import org.tensorflow.{DataType, Tensor}
import org.tensorflow.contrib.scala._
import org.tensorflow.contrib.scala.Rank._
import resource.Resource

/**
  * Supports TensorFlow tensors.
  */
object Tensors {

  /**
    * Converts a typed tensor to an untyped tensor.
    */
  implicit def untyped(t: TypedTensor[_,_]): Tensor = t.asInstanceOf[Tensor]

  /**
    * Type class to treat a [[Tensor]] instance as a managed resource.
    */
  implicit object tensorResource extends Resource[Tensor] {
    override def close(r: Tensor): Unit = r.close()
    override def toString: String = "Resource[Tensor]"
  }

  /**
    * Type class to treat a [[TypedTensor]] instance as a managed resource.
    */
  implicit def typedTensorResource: Resource[TypedTensor[_,_]] = new Resource[TypedTensor[_, _]] {
    override def close(r: TypedTensor[_, _]): Unit = r.close()
    override def toString: String = "Resource[TypedTensor[_,_]]"
  }

  /**
    * Type class to treat a product of [[Tensor]] instances as a managed resource.
    *
    * This type class facilitates the management of tensors within tuples and case classes.
    */
  implicit def productResource[P <: Product]: Resource[P] = {
    new Resource[P] {
      override def close(r: P): Unit =
        r.productIterator.collect { case t: Tensor => t }.foreach(_.close())
      override def toString: String = "Resource[Product]"
    }
  }
}
