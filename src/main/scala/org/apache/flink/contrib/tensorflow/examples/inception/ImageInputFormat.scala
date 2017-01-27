package org.apache.flink.contrib.tensorflow.examples.inception

import java.io.IOException
import java.util.Collections

import org.apache.flink.contrib.tensorflow.io.WholeFileInputFormat
import org.apache.flink.core.fs.{FSDataInputStream, Path}
import ImageNormalization.Image
import WholeFileInputFormat._
import org.apache.flink.api.common.io.GlobFilePathFilter
import org.apache.flink.configuration.Configuration

import scala.collection.JavaConverters._

/**
  * Input format for images.
  */
class ImageInputFormat extends WholeFileInputFormat[(String,Image)] {

  def this(includePattern: String) {
    this()
    setFilesFilter(new GlobFilePathFilter(List(includePattern).asJava, Collections.emptyList()))
  }


  override def configure(parameters: Configuration): Unit = {
    super.configure(parameters)
    setFilesFilter(new GlobFilePathFilter(List("**.jpg").asJava, List("**.crdownload").asJava))
  }

  /**
    * This function parses the given file stream which represents a serialized record.
    * The function returns a valid record or throws an IOException.
    *
    * @param reuse      An optionally reusable object.
    * @param fileStream The file input stream.
    * @return Returns the read record if it was successfully read.
    * @throws IOException if the record could not be read.
    */
  override def readRecord(reuse: (String,Image), filePath: Path, fileStream: FSDataInputStream, fileLength: Long): (String,Image) = {
    if(fileLength > Int.MaxValue) {
      throw new IllegalArgumentException("the file is too large to be fully read")
    }
    val name = filePath.getName
    reuse match {
      case (_: String, r: Array[Byte]) if r.length == fileLength => (name, readFully(fileStream, r, 0, r.length))
      case _ => (name, readFully(fileStream, new Array[Byte](fileLength.toInt), 0, fileLength.toInt))
    }
  }

//  override def readRecord(reuse: Image, filePath: Path, fileStream: FSDataInputStream, fileLength: Long): Image = {
//    if(fileLength > Int.MaxValue) {
//      throw new IllegalArgumentException("the file is too large to be fully read")
//    }
//    reuse match {
//      case r: Array[Byte] if r.length == fileLength => readFully(fileStream, r, 0, r.length)
//      case _ => readFully(fileStream, new Array[Byte](fileLength.toInt), 0, fileLength.toInt)
//    }
//  }
}
