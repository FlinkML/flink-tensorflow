package org.apache.flink.contrib.tensorflow.streaming

import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.streaming.api.scala.DataStream

import reflect.ClassTag


/**
  * Provides implicit conversions for {@link TF}.
  */
package object scala {

  /**
    * Enrich a [[DataStream]] to directly support TensorFlow processing.
    *
    * @param dataStream
    * @tparam T
    */
  implicit class RichDataStream[T: TypeInformation : ClassTag](dataStream: DataStream[T]) {

    /**
      * Create an TensorFlow stream based on the current [[DataStream]].
      *
      * @return an TensorFlow stream.
      */
    def flow(): scala.TensorFlowStream[T] = {
      scala.TF.flow(dataStream)
    }
  }

}
