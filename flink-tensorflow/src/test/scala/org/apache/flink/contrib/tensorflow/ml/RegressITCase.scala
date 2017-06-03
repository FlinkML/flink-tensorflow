package org.apache.flink.contrib.tensorflow.ml

import com.twitter.bijection.Conversion._
import org.apache.flink.api.common.functions.RichFlatMapFunction
import org.apache.flink.api.scala._
import org.apache.flink.configuration.Configuration
import org.apache.flink.contrib.tensorflow.ml.signatures.RegressionMethod._
import org.apache.flink.contrib.tensorflow.types.TensorInjections.{message2Tensor, messages2Tensor}
import org.apache.flink.contrib.tensorflow.util.TestData._
import org.apache.flink.contrib.tensorflow.util.{FlinkTestBase, RegistrationUtils}
import org.apache.flink.core.fs.Path
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.util.Collector
import org.apache.flink.util.Preconditions.checkState
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpecLike}
import org.tensorflow.Tensor
import org.tensorflow.contrib.scala.Arrays._
import org.tensorflow.contrib.scala.Rank._
import org.tensorflow.contrib.scala._
import org.tensorflow.example.Example
import resource._

@RunWith(classOf[JUnitRunner])
class RegressITCase extends WordSpecLike
  with Matchers
  with FlinkTestBase {

  override val parallelism = 1

  type LabeledExample = (Example, Float)

  def examples(): Seq[LabeledExample] = {
    for (v <- Seq(0.0f -> 2.0f, 1.0f -> 2.5f, 2.0f -> 3.0f, 3.0f -> 3.5f))
      yield (example("x" -> feature(v._1)), v._2)
  }

  "A RegressFunction" should {
    "process elements" in {
      val env = StreamExecutionEnvironment.getExecutionEnvironment
      RegistrationUtils.registerTypes(env.getConfig)

      val model = new HalfPlusTwo(new Path("../models/half_plus_two"))

      val outputs = env
        .fromCollection(examples())
        .flatMap(new RichFlatMapFunction[LabeledExample, Float] {
          override def open(parameters: Configuration): Unit = model.open()
          override def close(): Unit = model.close()

          override def flatMap(value: (Example, Float), out: Collector[Float]): Unit = {
            for {
              x <- managed(Seq(value._1).toList.as[Tensor].taggedAs[ExampleTensor])
              y <- model.regress_x_to_y(x)
            } {
              // cast as a 1D tensor to use the available conversion
              val o = y.taggedAs[TypedTensor[`1D`,Float]].as[Array[Float]]
              val actual = o(0)
              checkState(actual == value._2)
              out.collect(actual)
            }
          }
        })
        .print()

      env.execute()
    }
  }
}
