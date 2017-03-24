package org.apache.flink.contrib.tensorflow

import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.contrib.tensorflow.common.functions.AbstractMapFunction
import org.apache.flink.contrib.tensorflow.models.Model
import org.apache.flink.streaming.api.scala.DataStream

import scala.reflect.ClassTag

package object streaming {

  /**
    * Enrich a [[DataStream]] to directly support model-based transformation.
    */
  implicit class RichDataStream[T: TypeInformation : ClassTag](stream: DataStream[T]) {

    /**
      * Creates a new DataStream by applying the given function to every element of this
      * DataStream using the associated model.
      *
      * Supports models that implement [[org.apache.flink.contrib.tensorflow.models.RichModel]].
      */
    def mapWithModel[M <: Model[M], R: TypeInformation](model: M)(fun: (T,M) => R): DataStream[R] = {
      if (fun == null) {
        throw new NullPointerException("Map function must not be null.")
      }
      val m = model
      val cleanFun = clean(fun)
      val mapper = new AbstractMapFunction[T, R] {
        def model = m
        def map(in: T): R = cleanFun(in, m)
      }
      stream.map(mapper)
    }

    /**
      * Returns a "closure-cleaned" version of the given function. Cleans only if closure cleaning
      * is not disabled in the [[org.apache.flink.api.common.ExecutionConfig]].
      */
    private[tensorflow] def clean[F <: AnyRef](f: F): F = {
      stream.executionEnvironment.scalaClean(f)
    }
  }
}
