package org.apache.flink.contrib.tensorflow.examples.inception

import java.util.{List => JavaList}

import com.twitter.bijection.Conversion._
import com.twitter.bijection.{AbstractInjection, Injection}
import org.apache.flink.contrib.tensorflow.examples.common.GraphBuilder
import org.apache.flink.contrib.tensorflow.examples.inception.ImageNormalization._
import org.apache.flink.contrib.tensorflow.models.Model.RunContext
import org.apache.flink.contrib.tensorflow.models.Signature
import org.apache.flink.contrib.tensorflow.models.generic.{GenericModel, GraphDefGraphLoader, GraphLoader}
import org.apache.flink.contrib.tensorflow.types.TensorValue
import org.apache.flink.util.Preconditions.checkState
import org.slf4j.{Logger, LoggerFactory}
import org.tensorflow._

/**
  * Decodes and normalizes a JPEG image (as a byte[]) as a 4D tensor.
  *
  * <p>The output is compatible with inception5h.
  */
@SerialVersionUID(1L)
class ImageNormalization extends GenericModel[ImageNormalization] {

  protected val (graphDef, signature) = {
    try {
      val b: GraphBuilder = new GraphBuilder
      try {
        // Some constants specific to the pre-trained model at:
        // https://storage.googleapis.com/download.tensorflow.org/models/inception5h.zip
        //
        // - The model was trained with images scaled to 224x224 pixels.
        // - The colors, represented as R, G, B in 1-byte each were converted to
        //   float using (value - Mean)/Scale.
        val H: Int = 224
        val W: Int = 224
        val mean: Float = 117f
        val scale: Float = 1f

        // Since the graph is being constructed once per execution here, we can use a constant for the
        // input image. If the graph were to be re-used for multiple input images, a placeholder would
        // have been more appropriate.
        val input: Output = b.constant("input", INPUT_IMAGE_TEMPLATE)
        val output: Output = b.div(
          b.sub(
            b.resizeBilinear(
              b.expandDims(
                b.cast(b.decodeJpeg(input, 3), DataType.FLOAT),
                b.constant("make_batch", 0)),
              b.constant("size", Array[Int](H, W))),
            b.constant("mean", mean)),
          b.constant("scale", scale))

        val signature = new NormalizationSignature[ImageNormalization](input.op.name, output.op.name)

        (b.buildGraphDef(), signature)
      } finally {
        b.close()
      }
    }
  }


  override protected def graphLoader: GraphLoader = new GraphDefGraphLoader(graphDef)

  /**
    * Normalizes an image to a 4D tensor value.
    */
  def normalize: NormalizationSignature[ImageNormalization] = signature
}

@SerialVersionUID(1L)
class NormalizationSignature[M](inputName: String, outputName: String)
  extends Signature[M,Seq[Image],TensorValue] {

  override def run(model: M, context: RunContext, input: Seq[Image]): TensorValue = {

    // convert the input element to a tensor
    val i: Tensor = input.as[Tensor]

    // define the command to fetch the output tensor
    val command: Session#Runner = context.session.runner.feed(inputName, i).fetch(outputName)

    // run the command
    val o: JavaList[Tensor] = command.run

    // process the output
    checkState(o.size == 1)
    TensorValue.fromTensor(o.get(0))
  }
}

object ImageNormalization {

  private[inception] val LOG: Logger = LoggerFactory.getLogger(classOf[ImageNormalization])

  private[inception] val INPUT_IMAGE_TEMPLATE: Array[Byte] = new Array[Byte](86412)

  // the input image type
  type Image = Array[Byte]

  /**
    * Convert a {@code Seq[Image]} to a {@link Tensor} of type {@link DataType#STRING}.
    */
  private[inception] implicit def images2Tensor: Injection[Seq[Image], Tensor] =
    new AbstractInjection[Seq[Image], Tensor] {
      def apply(arrys: Seq[Image]) = {
        // TODO
        Tensor.create(arrys.head)
      }
      override def invert(t: Tensor) = ???
    }
}
