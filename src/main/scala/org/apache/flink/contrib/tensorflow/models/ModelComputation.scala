package org.apache.flink.contrib.tensorflow.models

import org.tensorflow.{Session, Tensor}
import org.tensorflow.framework.SignatureDef
import java.util.{Map => JavaMap}

import org.apache.flink.contrib.tensorflow.models.ModelComputation.ModelComputationResult
import org.apache.flink.contrib.tensorflow.types.TensorName

import scala.collection.JavaConverters._

/**
  * Represents a computation supported by a TensorFlow graph as defined by a [[SignatureDef]].
  */
class ModelComputation(signatureDef: SignatureDef) extends GraphComputation {

  def run(session: Session, inputs: JavaMap[String, Tensor]): GraphComputation.Result = {
    val runner = session.runner

    // map inputs according to the signaturedef
    signatureDef.getInputsMap.asScala.foreach { kv =>
      require(inputs.containsKey(kv._1), s"An input tensor named ${kv._1} must be provided for this computation.")
      val tensor = inputs.get(kv._1)
      val tensorName = TensorName(kv._2.getName)
      runner.feed(tensorName.name, tensorName.index, tensor)
    }

    // fetch the outputs defined by the signaturedef
    signatureDef.getOutputsMap.asScala.foreach { kv =>
      val tensorName = TensorName(kv._2.getName)
      runner.fetch(tensorName.name, tensorName.index)
    }

    // run the computation
    val runOutputs = runner.run()

    // map outputs according to the signaturedef
    val outputs = signatureDef.getOutputsMap.asScala.zip(runOutputs.asScala).map(f => (f._1._1, f._2))

    new ModelComputationResult(outputs)
  }
}

object ModelComputation {

  class ModelComputationResult(_outputs: collection.mutable.Map[String, Tensor]) extends GraphComputation.Result {
    override def outputs(): JavaMap[String, Tensor] = _outputs.asJava

    override def close(): Unit = {
      _outputs.foreach(_._2.close())
    }
  }

}
