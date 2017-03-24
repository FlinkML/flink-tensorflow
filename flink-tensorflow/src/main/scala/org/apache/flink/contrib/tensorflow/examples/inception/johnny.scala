package org.apache.flink.contrib.tensorflow.examples.inception

import java.nio.file.Paths

import org.apache.flink.cep.scala.CEP
import org.apache.flink.cep.scala.pattern.Pattern
import org.apache.flink.contrib.tensorflow.examples.inception.InceptionModel._
import org.apache.flink.contrib.tensorflow.streaming._
import org.apache.flink.streaming.api.functions.source.FileProcessingMode._
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.time.Time
import org.tensorflow.contrib.scala._
import resource._

import scala.collection.mutable
import scala.concurrent.duration._

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
    val imageStream = env
      .readFile(new ImageInputFormat, imagePath, PROCESS_CONTINUOUSLY, (1 second).toMillis)

    // 2. label the image using the TensorFlow 'inception' model
    implicit val inceptionModel = new InceptionModel(modelPath)

    val labelStream = imageStream.mapWithModel(inceptionModel) { (in, model) =>
      val labeled = managed(in._2.toTensor.taggedAs[ImageTensor])
        .flatMap(img => model.label(img))
        .acquireAndGet(label => label.toTextLabels())
      println(labeled.head)
      labeled.head
    }

    // 3. detect a certain time-based pattern representing a 'secret access code'
    val detectionPattern = Pattern
      .begin[LabeledImage]("first").where(img => labeled(img, "cheeseburger", .50f))
      .followedBy("second").where(img => labeled(img, "ladybug", .50f))
      .followedBy("third").where(img => labeled(img, "llama", .50f))
      .within(Time.seconds(60))

    val detectionStream = CEP
      .pattern(labelStream, detectionPattern)
      .select(
        (pattern, timestamp) => AccessDenied(pattern))(
        (pattern) => AccessGranted(pattern("first"), pattern("second"), pattern("third")))

    // print the detection events
    detectionStream.print()

    // execute program
    env.execute("Johnny")
  }

  def labeled(image: LabeledImage, label: String, confidence: Float = .90f): Boolean = {
    image.labels.exists(l => l._2.equalsIgnoreCase(label) && l._1 >= confidence)
  }

  case class AccessDenied(pattern: mutable.Map[String, LabeledImage])
  case class AccessGranted(first: LabeledImage, second: LabeledImage, third: LabeledImage) {
    override def toString: String = s"AccessGranted(${(first.labels.head, second.labels.head, third.labels.head)})"
  }

}

