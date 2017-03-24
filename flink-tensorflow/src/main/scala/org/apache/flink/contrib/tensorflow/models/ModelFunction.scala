package org.apache.flink.contrib.tensorflow.models


import org.apache.flink.contrib.tensorflow.graphs.GraphMethod
import org.apache.flink.contrib.tensorflow.types.TensorName
import org.tensorflow.Session
import org.tensorflow.Session.Run
import org.tensorflow.framework.SignatureDef
import resource._

import scala.collection.JavaConverters._

/**
  * A model function.
  *
  * @tparam T the graph method implemented by the function.
  */
trait ModelFunction[T <: GraphMethod] {
  /**
    * Apply the model function to the graph.
    *
    * @return the output values as provided by the method.
    */
  def apply(in: T#Input): ManagedResource[T#Output]
}

object ModelFunction {

  /**
    * A model function based on a [[SignatureDef]].
    *
    * A [[SignatureDef]] binds the function to a specific graph.
    */
  def apply[T <: GraphMethod](session: Session, signatureDef: SignatureDef)(implicit method: T): ModelFunction[T] = {
    require(session != null, "a session must be provided")
    require(signatureDef != null, "a signatureDef must be provided")
    require(method.name == signatureDef.getMethodName)

    new ModelFunction[T] {
      def apply(in: T#Input): ManagedResource[T#Output] = {
        require(in != null, "Input must be provided")

        // create a managed resource that lazily runs the graph
        val runResource = new AbstractManagedResource[Session.Run] {
          import scala.collection.JavaConverters._
          override protected def open: Run = {
            val runner = session.runner

            // map inputs according to the signaturedef
            val inputs = method.inputs(in.asInstanceOf[method.Input])
            signatureDef.getInputsMap.asScala.foreach { kv =>
              val tensor = inputs.getOrElse(kv._1,
                throw new IllegalArgumentException(s"An input tensor named ${kv._1} must be provided for this computation."))
              val tensorName = TensorName(kv._2.getName)
              runner.feed(tensorName.name, tensorName.index, tensor)
            }

            // fetch the outputs defined by the signaturedef
            signatureDef.getOutputsMap.asScala.foreach { kv =>
              val tensorName = TensorName(kv._2.getName)
              runner.fetch(tensorName.name, tensorName.index)
            }

            // run the computation
            runner.runAndFetchMetadata()
          }

          override protected def unsafeClose(handle: Run, errors: Option[Throwable]): Unit =
            handle.outputs.asScala.foreach(_.close())
        }

        runResource.map { run =>
          // map outputs according to the signaturedef
          val outputs = signatureDef.getOutputsMap.asScala.zip(run.outputs.asScala).map(f => (f._1._1, f._2)).toMap
          method.outputs(outputs)
        }
      }
    }
  }
}
