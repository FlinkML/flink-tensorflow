package org.apache.flink.contrib.tensorflow.examples.inception

import java.nio.file.{FileSystems, Files}

import org.apache.flink.streaming.api.scala._
import org.apache.flink.contrib.tensorflow.streaming.scala._

/**
  * A streaming image labeler, based on the 'inception5h' model.
  */
object Inception {
  def main(args: Array[String]) {

    //    lazy val appArgs = ParameterTool.fromArgs(args)
    if (args.length < 2) {
      System.out.println("Usage: Inception <model-dir> <image-path...>")
      System.exit(1)
    }
    val model: String = args.toSeq.head
    val images: Seq[Array[Byte]] = args.toSeq.tail.map(readImage)

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    val imageStream = env.fromCollection(images)

    val imageNormalization = new ImageNormalization()

    val resultStream = imageStream
      .flow()
      .withGraph(imageNormalization.buildGraph())
      .process(imageNormalization)

    // execute program
    env.execute("Inception")
  }

  def readImage(localPath: String): Array[Byte] = {
    Files.readAllBytes(FileSystems.getDefault().getPath(localPath))
  }
}
