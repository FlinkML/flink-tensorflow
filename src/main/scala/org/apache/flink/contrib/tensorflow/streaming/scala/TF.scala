package org.apache.flink.contrib.tensorflow.streaming.scala

import org.apache.flink.api.common.ExecutionConfig
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.api.scala.ClosureCleaner
import org.apache.flink.contrib.tensorflow.streaming.{functions => jfunc}
import org.apache.flink.contrib.tensorflow.{streaming => jstreaming}
import org.apache.flink.streaming.api.scala.DataStream
import org.tensorflow.framework.GraphDef

import scala.reflect.ClassTag

/**
  */
object TF {
  private[tensorflow] def clean[F <: AnyRef](f: F, checkSerializable: Boolean = true)(implicit config: ExecutionConfig): F = {
    if (config.isClosureCleanerEnabled) {
      ClosureCleaner.clean(f, checkSerializable)
    }
    ClosureCleaner.ensureSerializable(f)
    f
  }

  private[tensorflow] def wrapStream[T: TypeInformation : ClassTag](stream: jstreaming.TensorFlowStream[T]): TensorFlowStream[T] = {
    new TensorFlowStream(stream)
  }

  def flow[T: TypeInformation : ClassTag](input: DataStream[T]): TensorFlowStream[T] = {
    wrapStream(jstreaming.TF.flow(input.javaStream))
  }
}

final class TensorFlowStream[T: TypeInformation : ClassTag](jstream: jstreaming.TensorFlowStream[T]) {

  implicit val config = jstream.getExecutionEnvironment.getConfig

  def withGraph(graphDef: GraphDef): this.type = {
    jstream.withGraphDef(graphDef)
    this
  }

  /**
    * Process input elements to produce output elements from the TensorFlow stream.
    *
    * @param processFunction the flow function.
    * @tparam R the type of the output elements.
    * @return a new data stream.
    */
  def process[R: TypeInformation : ClassTag](processFunction: jfunc.GraphFunction[T, R]): DataStream[R] = {
    val outType: TypeInformation[R] = implicitly[TypeInformation[R]]
    new DataStream[R](jstream.process(processFunction, outType))
  }
}
