package org.apache.flink.contrib.tensorflow.ml

import com.twitter.bijection.Conversion._
import org.apache.flink.api.common.functions.RichFlatMapFunction
import org.apache.flink.api.scala._
import org.apache.flink.configuration.Configuration
import org.apache.flink.contrib.tensorflow.types.{TensorValue, TensorValueBuilder}
import org.apache.flink.contrib.tensorflow.util.{FlinkTestBase, RegistrationUtils}
import org.apache.flink.core.fs.Path
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.util.Collector
import org.apache.flink.util.Preconditions.checkState
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpecLike}
import org.tensorflow.contrib.scala.Arrays._
import org.tensorflow.contrib.scala.Rank._
import org.tensorflow.contrib.scala._

import resource.{managed, _}
import org.tensorflow.DataType

@RunWith(classOf[JUnitRunner])
class PredictITCase extends WordSpecLike
  with Matchers
  with FlinkTestBase {

  override val parallelism = 1

  type ImageTensorValue = TensorValue[`2D`, Float]
  type LabeledImageTensorValue = (ImageTensorValue, Int)


  def examples(): Seq[LabeledImageTensorValue] = {
    val num_samples = 5
    val samples : Array[Array[Float]] = new Array[Array[Float]](num_samples)
    val labels = new Array[Int](num_samples)
    labels(0) = 7
    labels(1) = 2
    labels(2) = 1
    labels(3) = 0
    labels(4) = 4
    samples(0) = Array(0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,84f,185f,159f,151f,60f,36f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,222f,254f,254f,254f,254f,241f,198f,198f,198f,198f,198f,198f,198f,198f,170f,52f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,67f,114f,72f,114f,163f,227f,254f,225f,254f,254f,254f,250f,229f,254f,254f,140f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,17f,66f,14f,67f,67f,67f,59f,21f,236f,254f,106f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,83f,253f,209f,18f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,22f,233f,255f,83f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,129f,254f,238f,44f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,59f,249f,254f,62f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,133f,254f,187f,5f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,9f,205f,248f,58f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,126f,254f,182f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,75f,251f,240f,57f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,19f,221f,254f,166f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,3f,203f,254f,219f,35f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,38f,254f,254f,77f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,31f,224f,254f,115f,1f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,133f,254f,254f,52f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,61f,242f,254f,254f,52f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,121f,254f,254f,219f,40f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,121f,254f,207f,18f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f)
    samples(1) = Array(0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,116f,125f,171f,255f,255f,150f,93f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,169f,253f,253f,253f,253f,253f,253f,218f,30f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,169f,253f,253f,253f,213f,142f,176f,253f,253f,122f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,52f,250f,253f,210f,32f,12f,0f,6f,206f,253f,140f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,77f,251f,210f,25f,0f,0f,0f,122f,248f,253f,65f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,31f,18f,0f,0f,0f,0f,209f,253f,253f,65f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,117f,247f,253f,198f,10f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,76f,247f,253f,231f,63f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,128f,253f,253f,144f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,176f,246f,253f,159f,12f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,25f,234f,253f,233f,35f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,198f,253f,253f,141f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,78f,248f,253f,189f,12f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,19f,200f,253f,253f,141f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,134f,253f,253f,173f,12f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,248f,253f,253f,25f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,248f,253f,253f,43f,20f,20f,20f,20f,5f,0f,5f,20f,20f,37f,150f,150f,150f,147f,10f,0f,0f,0f,0f,0f,0f,0f,0f,0f,248f,253f,253f,253f,253f,253f,253f,253f,168f,143f,166f,253f,253f,253f,253f,253f,253f,253f,123f,0f,0f,0f,0f,0f,0f,0f,0f,0f,174f,253f,253f,253f,253f,253f,253f,253f,253f,253f,253f,253f,249f,247f,247f,169f,117f,117f,57f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,118f,123f,123f,123f,166f,253f,253f,253f,155f,123f,123f,41f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f)
    samples(2) = Array(0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,38f,254f,109f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,87f,252f,82f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,135f,241f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,45f,244f,150f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,84f,254f,63f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,202f,223f,11f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,32f,254f,216f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,95f,254f,195f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,140f,254f,77f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,57f,237f,205f,8f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,124f,255f,165f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,171f,254f,81f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,24f,232f,215f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,120f,254f,159f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,151f,254f,142f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,228f,254f,66f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,61f,251f,254f,66f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,141f,254f,205f,3f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,10f,215f,254f,121f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,5f,198f,176f,10f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f)
    samples(3) = Array(0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,11f,150f,253f,202f,31f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,37f,251f,251f,253f,107f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,21f,197f,251f,251f,253f,107f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,110f,190f,251f,251f,251f,253f,169f,109f,62f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,253f,251f,251f,251f,251f,253f,251f,251f,220f,51f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,182f,255f,253f,253f,253f,253f,234f,222f,253f,253f,253f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,63f,221f,253f,251f,251f,251f,147f,77f,62f,128f,251f,251f,105f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,32f,231f,251f,253f,251f,220f,137f,10f,0f,0f,31f,230f,251f,243f,113f,5f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,37f,251f,251f,253f,188f,20f,0f,0f,0f,0f,0f,109f,251f,253f,251f,35f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,37f,251f,251f,201f,30f,0f,0f,0f,0f,0f,0f,31f,200f,253f,251f,35f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,37f,253f,253f,0f,0f,0f,0f,0f,0f,0f,0f,32f,202f,255f,253f,164f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,140f,251f,251f,0f,0f,0f,0f,0f,0f,0f,0f,109f,251f,253f,251f,35f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,217f,251f,251f,0f,0f,0f,0f,0f,0f,21f,63f,231f,251f,253f,230f,30f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,217f,251f,251f,0f,0f,0f,0f,0f,0f,144f,251f,251f,251f,221f,61f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,217f,251f,251f,0f,0f,0f,0f,0f,182f,221f,251f,251f,251f,180f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,218f,253f,253f,73f,73f,228f,253f,253f,255f,253f,253f,253f,253f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,113f,251f,251f,253f,251f,251f,251f,251f,253f,251f,251f,251f,147f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,31f,230f,251f,253f,251f,251f,251f,251f,253f,230f,189f,35f,10f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,62f,142f,253f,251f,251f,251f,251f,253f,107f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,72f,174f,251f,173f,71f,72f,30f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f)
    samples(4) = Array(0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,50f,224f,0f,0f,0f,0f,0f,0f,0f,70f,29f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,121f,231f,0f,0f,0f,0f,0f,0f,0f,148f,168f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,4f,195f,231f,0f,0f,0f,0f,0f,0f,0f,96f,210f,11f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,69f,252f,134f,0f,0f,0f,0f,0f,0f,0f,114f,252f,21f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,45f,236f,217f,12f,0f,0f,0f,0f,0f,0f,0f,192f,252f,21f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,168f,247f,53f,0f,0f,0f,0f,0f,0f,0f,18f,255f,253f,21f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,84f,242f,211f,0f,0f,0f,0f,0f,0f,0f,0f,141f,253f,189f,5f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,169f,252f,106f,0f,0f,0f,0f,0f,0f,0f,32f,232f,250f,66f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,15f,225f,252f,0f,0f,0f,0f,0f,0f,0f,0f,134f,252f,211f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,22f,252f,164f,0f,0f,0f,0f,0f,0f,0f,0f,169f,252f,167f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,9f,204f,209f,18f,0f,0f,0f,0f,0f,0f,22f,253f,253f,107f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,169f,252f,199f,85f,85f,85f,85f,129f,164f,195f,252f,252f,106f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,41f,170f,245f,252f,252f,252f,252f,232f,231f,251f,252f,252f,9f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,49f,84f,84f,84f,84f,0f,0f,161f,252f,252f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,127f,252f,252f,45f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,128f,253f,253f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,127f,252f,252f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,135f,252f,244f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,232f,236f,111f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,179f,66f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f)

    val shapeArray = Array(1L, 784L)

    val tvb = TensorValueBuilder.newBuilder()
    tvb.shape(shapeArray)
    tvb.dataType(DataType.FLOAT)

    for (i <- 0 to (num_samples - 1))
      yield (tvb.data(samples(i)).build().asInstanceOf[ImageTensorValue], labels(i))
  }


  "A PredictFunction" should {
    "process elements" in {
      val env = StreamExecutionEnvironment.getExecutionEnvironment
      RegistrationUtils.registerTypes(env.getConfig)

      val model = new MNIST_dense(new Path("../models/mnist_dense"))


      val outputs = env
        .fromCollection(examples())
        .flatMap(new RichFlatMapFunction[LabeledImageTensorValue, Int] {
          override def open(parameters: Configuration): Unit = model.open()
          override def close(): Unit = model.close()

          override def flatMap(value: LabeledImageTensorValue, out: Collector[Int]): Unit = {

            for {
              x <- managed(value._1.toTensor())
              y <- model.predict(x)
            } {
              // cast as a 1D tensor to use the available conversion
              val o = y.taggedAs[TypedTensor[`1D`,Float]].as[Array[Float]]

              // prediction is the index of the max value returned
              val max_index = o.zipWithIndex.maxBy(_._1)._2

              checkState(max_index == value._2)

              out.collect(max_index)
            }
          }
        })
        .print()

      env.execute()
    }
  }
}
