package org.apache.flink.contrib.tensorflow.models


import java.util
import java.util.Map

import org.apache.flink.contrib.tensorflow.types.TensorName
import org.tensorflow.{Session, Tensor}
import org.tensorflow.Session.Run
import org.tensorflow.framework.SignatureDef

import scala.collection.JavaConverters._
import resource._

/**
  * A model function.
  *
  * @tparam T the method type providing specific input and output type information.
  */
trait ModelFunction[T <: ModelMethod] {
  /**
    * Apply the model function to the graph.
    *
    * @param method the method associated with the computation, including input values.
    * @return the output values as provided by the method.
    */
  def apply(method: T): ManagedResource[method.Result]
}

object ModelFunction {

  /**
    * A model function based on a [[SignatureDef]].
    *
    * A [[SignatureDef]] binds the function to a specific graph.
    */
  def apply[T <: ModelMethod](session: Session, signatureDef: SignatureDef): ModelFunction[T] = {
    require(session != null, "a session must be provided")
    require(signatureDef != null, "a signatureDef must be provided")

    new ModelFunction[T] {
      def apply(method: T): ManagedResource[method.Result] = {
        require(method.name == signatureDef.getMethodName)

        // create a managed resource that lazily runs the graph
        val runResource = new AbstractManagedResource[Session.Run] {
          import scala.collection.JavaConverters._
          override protected def open: Run = {
            val runner = session.runner

            // map inputs according to the signaturedef
            val inputs = method.inputs()
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
