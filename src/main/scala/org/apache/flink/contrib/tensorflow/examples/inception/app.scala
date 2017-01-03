package org.apache.flink.contrib.tensorflow.examples.inception

import java.nio.file.{FileSystems, Files, Path}

import org.apache.flink.contrib.tensorflow.streaming.scala._
import org.apache.flink.streaming.api.scala._
import org.apache.flink.api.java.tuple.{Tuple2=>FlinkTuple2}

/**
  * A streaming image labeler, based on the 'inception5h' model.
  */
object Inception {

  def main(args: Array[String]) {
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    if (args.length < 2) {
      System.out.println("Usage: Inception <model-dir> <image-path...>")
      System.exit(1)
    }

    val modelPath: String = args.toSeq.head
    val images = args.toSeq.tail.map(readImage)

    val imageStream = env
      .fromCollection(images)
      .map(new ImageNormalization())

    val labelStream = imageStream
      .map(new InceptionModel(modelPath))

    labelStream.print()

    // execute program
    env.execute("Inception")
  }

  def readImage(localPath: String): FlinkTuple2[String,Array[Byte]] = {
    new FlinkTuple2(localPath, Files.readAllBytes(FileSystems.getDefault().getPath(localPath)))
  }
}
