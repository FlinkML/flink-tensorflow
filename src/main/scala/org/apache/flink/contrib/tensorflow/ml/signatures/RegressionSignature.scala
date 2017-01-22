package org.apache.flink.contrib.tensorflow.ml.signatures

import com.twitter.bijection.Conversion._
import com.twitter.bijection.{Conversion, Convert}
import org.apache.flink.contrib.tensorflow.ml.signatures.RegressionSignature.RegressionOutputs
import org.apache.flink.contrib.tensorflow.models.Model.RunContext
import org.apache.flink.contrib.tensorflow.models.savedmodel.SignatureConstants._
import org.apache.flink.contrib.tensorflow.models.{ModelComputation, Signature}
import org.apache.flink.contrib.tensorflow.types.TensorInjections._
import org.tensorflow.Tensor
import org.tensorflow.example.Example
import org.tensorflow.framework.{SignatureDef, TensorInfo}

import scala.collection.JavaConverters._
import org.apache.flink.contrib.tensorflow.types.ExampleBuilder._

/**
  * The standard regression method.
  *
  * See https://github.com/tensorflow/serving/blob/master/tensorflow_serving/servables/tensorflow/predict_impl.cc
  *
  * @param signatureDef the 'regress' signaturedef to bind to.
  */
@SerialVersionUID(1L)
class RegressionSignature[M](signatureDef: SignatureDef)
  extends Signature[M, Seq[Example], RegressionOutputs] {

  require(signatureDef.getMethodName == REGRESS_METHOD_NAME)

  override def run(model: M, context: RunContext, input: Seq[Example]): RegressionOutputs = {
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

@SerialVersionUID(1L)
@Deprecated // experimental
class RegressionSignatureWithFloat[M](signatureDef: SignatureDef)
  extends Signature[M, Float, Float] {

  require(signatureDef.getMethodName == REGRESS_METHOD_NAME)

  override def run(model: M, context: RunContext, input: Float): Float = {
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


object RegressionSignature {
  case class RegressionOutputs(output: Array[Float])

  type ToExample[T] = Conversion[T,Example]
  type OutputTensorLike[T] = Conversion[Tensor,Option[T]]
}

