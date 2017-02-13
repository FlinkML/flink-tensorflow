package org.apache.flink.contrib.tensorflow.ml

import org.apache.flink.api.common.functions.RichMapFunction
import org.apache.flink.api.scala._
import org.apache.flink.configuration.Configuration
import org.apache.flink.contrib.tensorflow.ml.models.HalfPlusTwo
import org.apache.flink.contrib.tensorflow.util.TestData._
import org.apache.flink.contrib.tensorflow.util.{FlinkTestBase, RegistrationUtils, TestData}
import org.apache.flink.core.fs.Path
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.util.Preconditions.checkState
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpecLike}
import org.tensorflow.example.Example

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

      val model = new HalfPlusTwo(new Path("file:///tmp/saved_model_half_plus_two"))

      val outputs = env
        .fromCollection(examples())
        .map(new RichMapFunction[LabeledExample, Float] {

          override def open(parameters: Configuration): Unit = model.open()

          override def close(): Unit = model.close()

          override def map(value: LabeledExample): Float = {
            val outputs = model.regress(Seq(value._1))
            val actual = outputs.output(0)
            checkState(actual == value._2)
            actual
          }
        })
        .print()

      env.execute()
    }
  }
}
