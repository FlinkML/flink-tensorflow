package org.apache.flink.contrib.tensorflow.ml.signatures

import org.apache.flink.contrib.tensorflow.graphs.GraphMethod
import org.apache.flink.contrib.tensorflow.models.savedmodel.SignatureConstants._
import org.tensorflow.Tensor

/**
  * The standard prediction signature.
  *
  * See https://github.com/tensorflow/serving/blob/master/tensorflow_serving/servables/tensorflow/predict_impl.cc
  */
sealed trait PredictionMethod extends GraphMethod {
  val name = PREDICT_METHOD_NAME
  override type Input = Tensor
  override type Output = Tensor
}

object PredictionMethod {

  /**
    * For each input tensor, output the score for each possible classification
    */
  implicit val impl = new PredictionMethod {
    type Result = Tensor
    def inputs(i: Tensor): Map[String, Tensor] = Map(PREDICT_INPUTS -> i)
    def outputs(o: Map[String, Tensor]): Tensor = o(PREDICT_OUTPUTS)
  }
}

