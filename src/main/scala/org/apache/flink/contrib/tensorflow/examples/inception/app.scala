package org.apache.flink.contrib.tensorflow.examples.inception

import java.nio.file.{FileSystems, Files, Path}

import org.apache.flink.contrib.tensorflow.streaming.scala._
import org.apache.flink.streaming.api.scala._
import org.apache.flink.api.java.tuple.{Tuple2 => FlinkTuple2}
import org.apache.flink.contrib.tensorflow.common.functions.AbstractMapFunction
import org.apache.flink.contrib.tensorflow.models.Model
import org.apache.flink.contrib.tensorflow.types.TensorValue
import org.apache.flink.contrib.tensorflow.streaming._

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

    val normalizationModel = new ImageNormalization()

    val imageStream = env
      .fromCollection(images)
      .map(new AbstractMapFunction[FlinkTuple2[String,Array[Byte]],FlinkTuple2[String,TensorValue]] {
        override def model: Model[_] = normalizationModel
        override def map(value: FlinkTuple2[String, Array[Byte]]): FlinkTuple2[String, TensorValue] = {
          val output = normalizationModel.run(Seq(value.f1))(normalizationModel.normalize)
          new FlinkTuple2(value.f0, output)
        }
      })

    imageStream.print()
//
//    val labelStream = imageStream
//      .map(new InceptionModel(modelPath))
//
//    labelStream.print()

    // execute program
    env.execute("Inception")
  }

  def readImage(localPath: String): FlinkTuple2[String,Array[Byte]] = {
    new FlinkTuple2(localPath, Files.readAllBytes(FileSystems.getDefault().getPath(localPath)))
  }
}
