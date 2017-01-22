package org.apache.flink.contrib.tensorflow.examples.inception

import java.nio.file.{FileSystems, Files}

import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.contrib.tensorflow.streaming._
import org.apache.flink.streaming.api.scala._

/**
  * A streaming image labeler, based on the 'inception5h' model.
  */
object Inception {

  type Image = Array[Byte]

  def main(args: Array[String]) {
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    if (args.length < 2) {
      System.out.println("Usage: Inception <model-dir> <image-path...>")
      System.exit(1)
    }
    val modelPath: String = args.toSeq.head
    val images = args.toSeq.tail.map(readImage)

    val normalizationModel = new ImageNormalization()

    val imageStream = env
      .fromCollection(images)
      .mapWithModel(normalizationModel) { (in, model) =>
        (in._1, model.run(Seq(in._2))(model.normalize))
      }

    imageStream.print()
//
//    val labelStream = imageStream
//      .map(new InceptionModel(modelPath))
//
//    labelStream.print()

    // execute program
    env.execute("Inception")
  }

  def readImage(localPath: String): (String,Image) = {
   (localPath, Files.readAllBytes(FileSystems.getDefault.getPath(localPath)))
  }
}
