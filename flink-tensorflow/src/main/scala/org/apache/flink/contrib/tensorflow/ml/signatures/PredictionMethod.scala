package org.apache.flink.contrib.tensorflow.ml.signatures

import com.twitter.bijection._
import org.apache.flink.contrib.tensorflow.models.savedmodel.SignatureConstants._
import org.apache.flink.contrib.tensorflow.types.TensorInjections._
import org.tensorflow.Tensor
import org.tensorflow.contrib.scala._
import org.tensorflow.contrib.scala.Rank._
import org.tensorflow.example.Example
import PredictionMethod._
import org.apache.flink.contrib.tensorflow.graphs.GraphMethod

/**
  * The standard prediction signature.
  *
  * See https://github.com/tensorflow/serving/blob/master/tensorflow_serving/servables/tensorflow/predict_impl.cc
  */
sealed trait PredictionMethod extends GraphMethod {
  val name = PREDICT_METHOD_NAME
  override type Input = PredictionInputTensor
  override type Output = PredictionOutputTensor
}

object PredictionMethod {
  // TODO: input and output types are not part of the standard signature so should be configurable.
  // This could be accomplished by:
  // 1) Defining the input/output types not here but in sub-classes. However, having a sub-class for every input/output
  //    configuration seems like overkill.
  // 2) making this a class that takes the types as input
  // 3) exposing an interface to set/override these values
  //
  // Similarly, the names of the inputs and outputs (PREDICT_INPUTS/PREDICT_OUTPUTS) have defaults in the signature
  // but can be overridden and still work in tf_serve.
  type PredictionInputTensor = TypedTensor[`2D`, Float]
  type PredictionOutputTensor = TypedTensor[`2D`, Float]

  /**
    * For each input tensor, output the score for each possible classification
    */
  implicit val impl = new PredictionMethod {
    type Result = PredictionOutputTensor
    def inputs(i: PredictionInputTensor): Map[String, Tensor] = Map(PREDICT_INPUTS -> i)
    def outputs(o: Map[String, Tensor]): PredictionOutputTensor = o(PREDICT_OUTPUTS).taggedAs[PredictionOutputTensor]
  }
}

