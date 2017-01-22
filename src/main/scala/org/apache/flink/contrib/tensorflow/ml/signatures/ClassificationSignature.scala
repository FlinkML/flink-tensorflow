package org.apache.flink.contrib.tensorflow.ml.signatures

import org.apache.flink.contrib.tensorflow.ml.signatures.ClassificationSignature.ClassificationOutputs
import org.apache.flink.contrib.tensorflow.models.Model.RunContext
import org.apache.flink.contrib.tensorflow.models.Signature
import org.apache.flink.contrib.tensorflow.models.savedmodel.SignatureConstants._
import org.apache.flink.ml.math.Vector
import org.tensorflow.example.Example
import org.tensorflow.framework.SignatureDef

/**
  * The standard classfication method.
  */
@SerialVersionUID(1L)
class ClassificationSignature[M](signatureDef: SignatureDef)
  extends Signature[M,Seq[Example], ClassificationOutputs] {

  require(signatureDef.getMethodName == CLASSIFY_METHOD_NAME)

  override def run(instance: M, context: RunContext, input: Seq[Example]): ClassificationOutputs = ???
}

object ClassificationSignature {
  case class ClassificationOutputs(classes: Array[Int], scores: Vector)
}
