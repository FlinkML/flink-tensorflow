package org.apache.flink.contrib.tensorflow.examples.inception

import java.net.URI
import java.nio.charset.StandardCharsets
import java.util.{List => JavaList}

import com.twitter.bijection.Conversion._
import org.apache.flink.contrib.tensorflow.examples.inception.InceptionModel._
import org.apache.flink.contrib.tensorflow.models.Model.RunContext
import org.apache.flink.contrib.tensorflow.models.Signature
import org.apache.flink.contrib.tensorflow.models.generic.{DefaultGraphLoader, GenericModel, GraphLoader}
import org.apache.flink.contrib.tensorflow.types.Rank._
import org.apache.flink.contrib.tensorflow.types.TensorInjections._
import org.apache.flink.contrib.tensorflow.types.TensorValue
import org.apache.flink.contrib.tensorflow.util.GraphUtils
import org.apache.flink.core.fs.Path
import org.slf4j.{Logger, LoggerFactory}
import org.tensorflow.framework.{SignatureDef, TensorInfo}

import scala.collection.JavaConverters._
import ImageLabelingSignature._

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

  /**
    * Convert the label tensor to a list of labels.
    */
  def labeled(tensor: LabelTensor, take: Int = 3): Array[LabeledImage] = {
    // the tensor consists of a row per image, with columns representing label probabilities
    val t = tensor.toTensor
    try {
      require(t.numDimensions() == 2, "expected a [M N] shaped tensor")
      val matrix = Array.ofDim[Float](t.shape()(0).toInt,t.shape()(1).toInt)
      t.copyTo(matrix)
      matrix.map { row =>
        LabeledImage(row.toList.zip(labels).sortWith(_._1 > _._1).take(take))
      }
    }
    finally {
      t.close()
    }
  }

  private val signatureDef = SignatureDef.newBuilder()
    .setMethodName("label")
    .putInputs(INFER_INPUTS, TensorInfo.newBuilder().setName("input").build())
    .putOutputs(INFER_OUTPUTS, TensorInfo.newBuilder().setName("output").build())
    .build()

  /**
    * Label an image according to the inception model.
    */
  def label[IN, OUT](input: IN)(implicit signature: ImageLabelingSignature[InceptionModel, IN, OUT]): OUT = {
    run(signature.run(this, signatureDef, _, input))
  }
}


trait ImageLabelingSignature[M, IN, OUT] extends Signature[M] {
  def run(model: M, signatureDef: SignatureDef, context: RunContext, input: IN): OUT
}

@SerialVersionUID(1L)
object ImageLabelingSignature {

  val INFER_INPUTS = "inputs"
  val INFER_OUTPUTS = "outputs"

  implicit def infer[M] = new ImageLabelingSignature[M, ImageTensor, LabelTensor] {
    override def run(model: M, signatureDef: SignatureDef, context: RunContext, input: ImageTensor): LabelTensor = {
      val i = input.toTensor
      try {
        val cmd = context.session.runner().feed("input", i).fetch("output")
        val o = cmd.run()
        try {
          o.get(0).as[Option[TensorValue[`2D`,Float]]].getOrElse(error("expected an output tensor of [2D,Float]"))
        }
        finally {
          o.asScala.foreach(_.close())
        }
      }
      finally {
        i.close()
      }
    }
  }
}

object InceptionModel {

  /**
    * A set of labels encoded a 2-D tensor of floats.
    */
  type LabelTensor = TensorValue[`2D`,Float]

  /**
    * An image with associated labels (sorted by probability descending)
    */
  case class LabeledImage(labels: List[(Float,String)])
}

