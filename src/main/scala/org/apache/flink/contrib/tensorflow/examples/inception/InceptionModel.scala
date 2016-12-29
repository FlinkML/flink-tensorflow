package org.apache.flink.contrib.tensorflow.examples.inception

import java.io.FileInputStream
import java.nio.file.{Files, Paths}
import java.util

import org.apache.flink.contrib.tensorflow.common.TensorValue
import org.apache.flink.contrib.tensorflow.streaming.functions.GraphFunction.{Context, OnTimerContext}
import org.apache.flink.contrib.tensorflow.streaming.functions.RichGraphFunction
import org.apache.flink.util.Collector
import org.slf4j.{Logger, LoggerFactory}
import org.tensorflow.framework.GraphDef

import scala.collection.JavaConverters._

/**
  * @author Eron Wright
  */
class InceptionModel(modelDir: String) extends RichGraphFunction[TensorValue,Inference] {

  protected val LOG: Logger = LoggerFactory.getLogger(classOf[InceptionModel])

  val labels = {
    Files.readAllLines(Paths.get(modelDir, "imagenet_comp_graph_label_strings.txt")).asScala.toList
  }

  def buildGraph: GraphDef = {
    val fs = new FileInputStream(Paths.get(modelDir, "tensorflow_inception_graph.pb").toFile)
    try {
      GraphDef.parseFrom(fs)
    }
    finally {
      fs.close()
    }
  }

  override def processElement(value: TensorValue, ctx: Context, out: Collector[Inference]): Unit = {
    val cmd = ctx.session().runner().feed("input", value.toTensor).fetch("output")
    val result = cmd.run().get(0)

    val rshape = result.shape
    if (result.numDimensions != 2 || rshape(0) != 1)
      throw new RuntimeException(String.format("Expected model to produce a [1 N] shaped tensor where N is the number of labels, instead it produced one with shape %s", util.Arrays.toString(rshape)))
    val nlabels = rshape(1).toInt
    val inferenceMatrix = Array.ofDim[Float](1,nlabels)
    result.copyTo(inferenceMatrix)

    val inference = toInference(inferenceMatrix)
    out.collect(inference)

    LOG.info(s"Inception($value) => $inference")
  }

  override def onTimer(timestamp: Long, ctx: OnTimerContext, out: Collector[Inference]): Unit = {

  }

  private def toInference(inferenceMatrix: Array[Array[Float]]): Inference = {
    Inference(inferenceMatrix(0).toList.zip(labels).sortWith(_._1 > _._1).take(5))
  }
}

case class Inference(inferences: List[(Float,String)]) {

}

