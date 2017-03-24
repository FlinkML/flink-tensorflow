package org.apache.flink.contrib.tensorflow.examples.inception

import java.io.IOException
import java.util.Collections

import com.twitter.bijection.Conversion._
import org.apache.flink.api.common.io.GlobFilePathFilter
import org.apache.flink.configuration.Configuration
import org.apache.flink.contrib.tensorflow._
import org.apache.flink.contrib.tensorflow.common.functions.util.ModelUtils
import org.apache.flink.contrib.tensorflow.io.WholeFileInputFormat
import org.apache.flink.contrib.tensorflow.io.WholeFileInputFormat._
import org.apache.flink.core.fs.{FSDataInputStream, Path}
import org.slf4j.{Logger, LoggerFactory}
import org.tensorflow.contrib.scala.ByteStrings._
import resource._

import scala.collection.JavaConverters._

/**
  * Input format for images.
  */
@SerialVersionUID(1L)
class ImageInputFormat extends WholeFileInputFormat[(String,ImageTensorValue)] {

  protected val LOG: Logger = LoggerFactory.getLogger(classOf[ImageInputFormat])

  def this(includePattern: String) {
    this()
    setFilesFilter(new GlobFilePathFilter(List(includePattern).asJava, Collections.emptyList()))
  }

  /**
    * The image normalization model that transforms JPEG files to 4D tensors
    */
  @transient var model: ImageNormalization = _

  override def openInputFormat(): Unit = {
    super.openInputFormat()
    model = new ImageNormalization()
    ModelUtils.openModel(model)
  }

  override def closeInputFormat(): Unit = {
    ModelUtils.closeModel(model)
    super.closeInputFormat()
  }

  override def configure(parameters: Configuration): Unit = {
    super.configure(parameters)
    setFilesFilter(new GlobFilePathFilter(List("**.jpg", "**.jpeg").asJava, List("**.crdownload").asJava))
  }

  /**
    * This function parses the given file stream which represents a raw image.
    * The function returns a valid image tensor value or throws an IOException.
    *
    * @param reuse      An optionally reusable object.
    * @param fileStream The file input stream.
    * @return Returns the image tensor value if it was successfully read.
    * @throws IOException if the image could not be read.
    */
  override def readRecord(
       reuse: (String,ImageTensorValue),
       filePath: Path, fileStream: FSDataInputStream,
       fileLength: Long): (String,ImageTensorValue) = {

    if(fileLength > Int.MaxValue) {
      throw new IllegalArgumentException("the file is too large to be fully read")
    }
    val imageData =
      readFully(fileStream, new Array[Byte](fileLength.toInt), 0, fileLength.toInt).asByteString[ImageFile]

    val imageTensor: ImageTensorValue =
      managed(imageData.as[ImageFileTensor])
      .flatMap(x => model.normalize(x))
      .acquireAndGet(_.toValue)

    (filePath.getName, imageTensor)
  }
}

object ImageInputFormat {
  def apply(): ImageInputFormat = new ImageInputFormat
}
