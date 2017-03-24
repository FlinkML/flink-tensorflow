package org.apache.flink.contrib.tensorflow.examples.inception

import java.net.URI
import java.nio.charset.StandardCharsets

import org.tensorflow.contrib.scala._
import com.twitter.bijection.Conversion._
import org.apache.flink.contrib.tensorflow.examples.inception.LabelMethod._
import org.apache.flink.contrib.tensorflow.graphs.{DefaultGraphLoader, GraphLoader}
import org.apache.flink.contrib.tensorflow.models.generic.GenericModel
import org.apache.flink.contrib.tensorflow.models.{ModelFunction, ModelMethod}
import org.apache.flink.contrib.tensorflow.types.TensorInjections._
import org.apache.flink.contrib.tensorflow.util.GraphUtils
import org.apache.flink.core.fs.Path
import org.slf4j.{Logger, LoggerFactory}
import org.tensorflow.Tensor
import org.tensorflow.framework.{SignatureDef, TensorInfo}

import scala.collection.JavaConverters._

/**
  * Infers labels for images.
  *
  * @param modelPath the directory containing the model files.
  */
@SerialVersionUID(1L)
class InceptionModel(modelPath: URI) extends GenericModel[InceptionModel] {

  protected val LOG: Logger = LoggerFactory.getLogger(classOf[InceptionModel])

  override protected def graphLoader: GraphLoader =
    new DefaultGraphLoader(new Path(new Path(modelPath), "tensorflow_inception_graph.pb"))

  @transient lazy val labels: List[String] = GraphUtils.readAllLines(
    new Path(new Path(modelPath), "imagenet_comp_graph_label_strings.txt"), StandardCharsets.UTF_8).asScala.toList


  private val signatureDef = SignatureDef.newBuilder()
    .setMethodName(LABEL_METHOD_NAME)
    .putInputs(LABEL_INPUTS, TensorInfo.newBuilder().setName("input").build())
    .putOutputs(LABEL_OUTPUTS, TensorInfo.newBuilder().setName("output").build())
    .build()

  /**
    * Infers labels for an image.
    */
  def label = ModelFunction[LabelMethod](session, signatureDef)
}

sealed trait LabelMethod extends ModelMethod {
  def name = LABEL_METHOD_NAME
}

@SerialVersionUID(1L)
object LabelMethod {
  val LABEL_METHOD_NAME = "inception/label"
  val LABEL_INPUTS = "inputs"
  val LABEL_OUTPUTS = "outputs"

  /**
    * Labels a tensor of normalized images as a tensor of labels (confidence scores).
    * @param input the images as a [[ImageTensor]]
    * @return the labels as a [[LabelTensor]]
    */
  implicit def fromImages(input: ImageTensor) =
    new LabelMethod {
      type Result = LabelTensor
      def inputs(): Map[String, Tensor] = Map(LABEL_INPUTS -> input)
      def outputs(o: Map[String, Tensor]): Result = o(LABEL_OUTPUTS).taggedAs[LabelTensor]
    }
}

object InceptionModel {
  /**
    * An image with associated labels (sorted by probability descending)
    */
  case class LabeledImage(labels: List[(Float,String)])

  implicit class RichLabelTensor(t: LabelTensor) {

    /**
      * Convert the label tensor to a list of labels.
      */
    def toTextLabels(take: Int = 3)(implicit model: InceptionModel): Array[LabeledImage] = {
      // the tensor consists of a row per image, with columns representing label probabilities
      require(t.numDimensions() == 2, "expected a [M N] shaped tensor")
      val matrix = Array.ofDim[Float](t.shape()(0).toInt,t.shape()(1).toInt)
      t.copyTo(matrix)
      matrix.map { row =>
        LabeledImage(row.toList.zip(model.labels).sortWith(_._1 > _._1).take(take))
      }
    }
  }

}

