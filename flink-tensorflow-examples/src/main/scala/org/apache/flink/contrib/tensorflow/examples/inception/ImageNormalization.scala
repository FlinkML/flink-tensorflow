package org.apache.flink.contrib.tensorflow.examples.inception

import org.apache.flink.contrib.tensorflow.examples.common.GraphBuilder
import org.apache.flink.contrib.tensorflow.examples.inception.ImageNormalization._
import org.apache.flink.contrib.tensorflow.examples.inception.ImageNormalizationMethod._
import org.apache.flink.contrib.tensorflow.graphs.{GraphDefGraphLoader, GraphLoader, GraphMethod}
import org.apache.flink.contrib.tensorflow.models.generic.GenericModel
import org.apache.flink.contrib.tensorflow.models.ModelFunction
import org.slf4j.{Logger, LoggerFactory}
import org.tensorflow._
import org.tensorflow.contrib.scala._
import org.tensorflow.framework.{SignatureDef, TensorInfo}

sealed trait ImageNormalizationMethod extends GraphMethod {
  val name = NORMALIZE_METHOD_NAME
  override type Input = ImageFileTensor
  override type Output = ImageTensor
}

object ImageNormalizationMethod {
  val NORMALIZE_METHOD_NAME = "inception/normalize"
  val NORMALIZE_INPUTS = "inputs"
  val NORMALIZE_OUTPUTS = "outputs"

  /**
    * Normalizes a vector of image files to a vector of images.
    */
  implicit val impl = new ImageNormalizationMethod {
    def inputs(i: Input): Map[String, Tensor] = Map(NORMALIZE_INPUTS -> i)
    def outputs(o: Map[String, Tensor]): Output = o(NORMALIZE_OUTPUTS).taggedAs[ImageTensor]
  }
}

/**
  * Decodes and normalizes a JPEG image as a 4D tensor.
  *
  * The output is compatible with inception5h.
  */
@SerialVersionUID(1L)
class ImageNormalization extends GenericModel[ImageNormalization] {

  protected val (graphDef, signatureDef) = {
    val b: GraphBuilder = new GraphBuilder
    try {
      // Some constants specific to the pre-trained model at:
      // https://storage.googleapis.com/download.tensorflow.org/models/inception5h.zip
      //
      // - The inception model was trained with images scaled to 224x224 pixels.
      // - The colors, represented as R, G, B in 1-byte each were converted to
      //   float using (value - Mean)/Scale.
      val H: Int = 224
      val W: Int = 224
      val mean: Float = 117f
      val scale: Float = 1f

      val input: Output = b.placeholder("input", DataType.STRING, Shape.scalar())
      val output: Output = b.div(
        b.sub(
          b.resizeBilinear(
            b.expandDims(
              b.cast(b.decodeJpeg(input, 3), DataType.FLOAT),
              b.constant("make_batch", 0)),
            b.constant("size", Array[Int](H, W))),
          b.constant("mean", mean)),
        b.constant("scale", scale))

      val signatureDef = SignatureDef.newBuilder()
        .setMethodName(NORMALIZE_METHOD_NAME)
        .putInputs(NORMALIZE_INPUTS, TensorInfo.newBuilder().setName(input.op.name).build())
        .putOutputs(NORMALIZE_OUTPUTS, TensorInfo.newBuilder().setName(output.op.name).build())
        .build()

      (b.buildGraphDef(), signatureDef)
    } finally {
      b.close()
    }
  }

  override protected def graphLoader: GraphLoader = new GraphDefGraphLoader(graphDef)

  /**
    * Normalizes an image to a 4-D tensor value.
    */
  def normalize = ModelFunction[ImageNormalizationMethod](session, signatureDef)
}

object ImageNormalization {

  private[inception] val LOG: Logger = LoggerFactory.getLogger(classOf[ImageNormalization])

}
