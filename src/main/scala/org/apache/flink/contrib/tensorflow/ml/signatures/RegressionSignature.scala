package org.apache.flink.contrib.tensorflow.ml.signatures

import com.twitter.bijection.Conversion._
import com.twitter.bijection.{Conversion, Convert}
import org.apache.flink.contrib.tensorflow.ml.signatures.RegressionSignature.RegressionOutputs
import org.apache.flink.contrib.tensorflow.models.Model.RunContext
import org.apache.flink.contrib.tensorflow.models.savedmodel.SignatureConstants._
import org.apache.flink.contrib.tensorflow.models.savedmodel.TensorFlowModel
import org.apache.flink.contrib.tensorflow.models.{ModelComputation, Signature}
import org.apache.flink.contrib.tensorflow.types.TensorInjections._
import org.tensorflow.Tensor
import org.tensorflow.example.Example
import org.tensorflow.framework.{SignatureDef, TensorInfo}

import scala.collection.JavaConverters._
import org.apache.flink.contrib.tensorflow.types.ExampleBuilder._


/**
  * The magnet for regression signature implementations.
  *
  * All regression signatures are executable as normal signatures.  This magnet allows for variants
  * of the input parameters to the regression signature.
  *
  * Keep in mind that the model user selects the specific implementation based on the input/output parameter types.
  */

/**
  * The standard regression method.
  *
  * See https://github.com/tensorflow/serving/blob/master/tensorflow_serving/servables/tensorflow/predict_impl.cc
  *
  */
trait RegressionSignature[M, IN, OUT] extends Signature[M] {
  def run(model: M, signatureDef: SignatureDef, context: RunContext, input: IN): OUT
}

object RegressionSignature {

  case class RegressionOutputs(output: Array[Float])

  type ToExample[T] = Conversion[T,Example]
  type OutputTensorLike[T] = Conversion[Tensor,Option[T]]

  /**
    * Regression signature for a sequence of examples.
    */
  implicit def regressWithSeqOfExamples[M] = new RegressionSignature[M, Seq[Example], RegressionOutputs] {
    override def run(model: M, signatureDef: SignatureDef, context: RunContext, input: Seq[Example]): RegressionOutputs = {
      require(signatureDef.getMethodName == REGRESS_METHOD_NAME)
      val c = new ModelComputation(signatureDef)

      // convert the list of examples to a tensor of DataType.STRING
      val i: Tensor = input.toList.as[Tensor]
      val result = c.run(context.session, Map(REGRESS_INPUTS -> i).asJava)
      try {
        // convert the tensor to a Array[Float]
        val o = result.outputs().get(REGRESS_OUTPUTS)
        val outputs: Array[Float] = o.as[Option[Array[Float]]].get
        RegressionOutputs(outputs)
      }
      finally {
        result.close()
      }
    }
  }

  @Deprecated // experimental
  implicit def regressWithFloat[M] = new RegressionSignature[M, Float, Float] {
    override def run(model: M, signatureDef: SignatureDef, context: RunContext, input: Float): Float = {
      val c = new ModelComputation(signatureDef)

      // convert the list of examples to a tensor of DataType.STRING
      val i: Tensor = example("x" -> feature(input)).as[Tensor]
      val result = c.run(context.session, Map(REGRESS_INPUTS -> i).asJava)
      try {
        // convert the tensor to a Array[Float]
        val o = result.outputs().get(REGRESS_OUTPUTS)
        val outputs: Array[Float] = o.as[Option[Array[Float]]].get
        outputs.head
      }
      finally {
        result.close()
      }
    }
  }
}

