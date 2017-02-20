package org.apache.flink.contrib.tensorflow.models


import org.tensorflow.Session
import org.tensorflow.framework.SignatureDef

import scala.collection.JavaConverters._

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
  def apply(method: T): method.Result
}

object ModelFunction {

  /**
    * A model function based on a [[SignatureDef]].
    *
    * A [[SignatureDef]] binds the function to a specific graph.
    */
  def apply[T <: ModelMethod](session: Session, signatureDef: SignatureDef): ModelFunction[T] = {
    new ModelFunction[T] {
      def apply(method: T): method.Result = {
        require(method.name == signatureDef.getMethodName)
        val inputs = method.inputs()
        try {
          val c = new ModelComputation(signatureDef)
          val result = c.run(session, inputs.asJava)
          try {
            val outputs = result.outputs().asScala.toMap
            method.outputs(outputs)
          }
          finally {
            result.close()
          }
        }
        finally {
          inputs.mapValues(_.close())
        }
      }
    }
  }
}
