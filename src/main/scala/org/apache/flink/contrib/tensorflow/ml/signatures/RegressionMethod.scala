package org.apache.flink.contrib.tensorflow.ml.signatures

import com.twitter.bijection.Conversion._
import org.apache.flink.contrib.tensorflow.models.ModelMethod
import org.apache.flink.contrib.tensorflow.models.savedmodel.SignatureConstants._
import org.apache.flink.contrib.tensorflow.types.Rank.`2D`
import org.apache.flink.contrib.tensorflow.types.TensorInjections._
import org.apache.flink.contrib.tensorflow.types.TensorValue
import org.tensorflow.Tensor
import org.tensorflow.example.Example

/**
  * The standard regression method.
  *
  * See https://github.com/tensorflow/serving/blob/master/tensorflow_serving/servables/tensorflow/predict_impl.cc
  */
sealed trait RegressionMethod extends ModelMethod {
  val name = REGRESS_METHOD_NAME
}

object RegressionMethod {
  type ExampleTensor = TensorValue[`2D`,Example]
  type PredictionTensor = TensorValue[`2D`,Float]

  /**
    * Predict a vector of values from a given vector of examples.
    * @param input the examples as a 2-D tensor of [[Example]]s.
    * @return a 2-D tensor of [[Float]]s with dimensions [-1,1]
    */
  implicit def fromExampleTensor(input: ExampleTensor) =
    new RegressionMethod {
      type Result = PredictionTensor
      def inputs(): Map[String, Tensor] = Map(REGRESS_INPUTS -> input.toTensor)
      def outputs(o: Map[String, Tensor]): Result = o(REGRESS_OUTPUTS).as[Option[PredictionTensor]].get
    }
}
