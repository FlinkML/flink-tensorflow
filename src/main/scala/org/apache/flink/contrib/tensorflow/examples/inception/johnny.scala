package org.apache.flink.contrib.tensorflow.examples.inception

import java.nio.file.{FileSystems, Files, Paths}

import org.apache.flink.cep.scala.{CEP, PatternStream}
import org.apache.flink.cep.scala.pattern.Pattern
import org.apache.flink.streaming.api.scala._
import org.apache.flink.contrib.tensorflow.examples.inception.InceptionModel.LabeledImage
import org.apache.flink.contrib.tensorflow.streaming._
import org.apache.flink.contrib.tensorflow.types.Rank.{`2D`, `4D`}
import org.apache.flink.contrib.tensorflow.types.TensorValue
import org.apache.flink.streaming.api.functions.source.FileProcessingMode._
import org.apache.flink.streaming.api.windowing.time.Time

import scala.collection.mutable
import scala.concurrent.duration._
import scala.util.Try

/**
  * Identifies specific image sequences, based on the 'inception5h' model.
  */
object Johnny {

  type Image = Array[Byte]

  def main(args: Array[String]) {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)

    if (args.length < 2) {
      System.out.println("Usage: Johnny <model-dir> <image-dir>")
      System.exit(1)
    }
    val modelPath = Paths.get(args.toSeq.head).toUri
    val imagePath = args.toSeq.tail.head

    // 1. read input files as images
    val fileStream = env.readFile(new ImageInputFormat, imagePath, PROCESS_CONTINUOUSLY, (1 second).toMillis)

    // 2. normalize the raw image as a 4D image tensor
    val normalizationModel = new ImageNormalization()
    val imageStream =
      fileStream.mapWithModel(normalizationModel) { (in, model) =>
        (in._1, model.run(Seq(in._2))(model.normalize))
      }

    // 3. label the image using the TensorFlow 'inception' model
    val inceptionModel = new InceptionModel(modelPath)
    val labelStream: DataStream[LabeledImage] = imageStream
      .mapWithModel(inceptionModel) { (in, model) =>
        val labelTensor = model.run(in._2)(model.label)
        val labeled = model.labeled(labelTensor, take = 3).head
        println(s"Processed: $labeled")
        labeled
      }

    // 4. apply a time-based detection pattern
    val detectionPattern = Pattern
      .begin[LabeledImage]("first").where(img => labeled(img, "cheeseburger", .50f))
      .followedBy("second").where(img => labeled(img, "ladybug", .50f))
      .followedBy("third").where(img => labeled(img, "llama", .50f))
      .within(Time.seconds(60))

    val detectionStream = CEP
      .pattern(labelStream, detectionPattern)
      .select(
        (pattern, timestamp) => BadSequence(pattern))(
        (pattern) => GoodSequence(pattern("first"), pattern("second"), pattern("third")))

    detectionStream.print()

    // execute program
    env.execute("Johnny")
  }

  def labeled(image: LabeledImage, label: String, confidence: Float = .90f): Boolean = {
    image.labels.exists(l => l._2.equalsIgnoreCase(label) && l._1 >= confidence)
  }

  case class BadSequence(pattern: mutable.Map[String, LabeledImage])
  case class GoodSequence(first: LabeledImage, second: LabeledImage, third: LabeledImage) {
    override def toString: String = s"GoodSequence(${(first.labels.head, second.labels.head, third.labels.head)})"
  }

}

