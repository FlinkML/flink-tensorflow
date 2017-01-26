package org.apache.flink.contrib.tensorflow.examples.inception

import java.nio.file.{FileSystems, Files, Paths}

import org.apache.flink.streaming.api.scala._
import org.apache.flink.contrib.tensorflow.examples.inception.InceptionModel.LabeledImage
import org.apache.flink.contrib.tensorflow.streaming._
import org.apache.flink.contrib.tensorflow.types.Rank.`4D`
import org.apache.flink.contrib.tensorflow.types.TensorValue
import org.apache.flink.streaming.api.functions.source.FileProcessingMode.PROCESS_CONTINUOUSLY
import scala.concurrent.duration._

/**
  * A streaming image labeler, based on the 'inception5h' model.
  */
object Inception {

  type Image = Array[Byte]

  def main(args: Array[String]) {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)

    if (args.length < 2) {
      System.out.println("Usage: Inception <model-dir> <images-dir>")
      System.exit(1)
    }
    val modelPath = Paths.get(args(0)).toUri
    val imagesPath = args(1)

    val fileStream = env.readFile(new ImageInputFormat, imagesPath, PROCESS_CONTINUOUSLY, (1 second).toMillis)

    // normalize the raw image as a 4D image tensor
    val normalizationModel = new ImageNormalization()

    val imageStream: DataStream[(String, TensorValue[`4D`,Float])] =
      fileStream.mapWithModel(normalizationModel) { (in, model) =>
        (in._1, model.run(Seq(in._2))(model.normalize))
      }

    // label the image tensor using the inception5h model
    val inceptionModel = new InceptionModel(modelPath)

    val labelStream: DataStream[(String,LabeledImage)] = imageStream
      .mapWithModel(inceptionModel) { (in, model) =>
        val labelTensor = model.run(in._2)(model.label)
        (in._1, model.labeled(labelTensor).head)
      }

    labelStream.print()

    // execute program
    env.execute("Inception")
  }
}

