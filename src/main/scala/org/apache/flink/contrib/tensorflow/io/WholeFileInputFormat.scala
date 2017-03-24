package org.apache.flink.contrib.tensorflow.io

import java.io.{EOFException, IOException, InputStream}

import org.apache.flink.api.common.io.FileInputFormat
import org.apache.flink.configuration.Configuration
import org.apache.flink.core.fs._
import org.apache.flink.util.Preconditions.checkState

/**
  * Reads whole files as input records.
  */
@SerialVersionUID(1L)
abstract class WholeFileInputFormat[T] extends FileInputFormat[T] {

  this.unsplittable = true

  override def configure(parameters: Configuration) {
    super.configure(parameters)
  }

  override protected def testForUnsplittable(pathFile: FileStatus): Boolean = {
    false
  }

  // --------------------------------------------------------------------------------------------
  //  User-defined behavior
  // --------------------------------------------------------------------------------------------
  /**
    * This function parses the given file stream which represents a serialized record.
    * The function returns a valid record or throws an IOException.
    *
    * @param reuse    An optionally reusable object.
    * @param fileStream The file input stream.
    * @return Returns the read record if it was successfully deserialized.
    * @throws IOException if the record could not be read.
    */
  @throws[IOException]
  def readRecord(reuse: T, filePath: Path, fileStream: FSDataInputStream, fileLength: Long): T

  // --------------------------------------------------------------------------------------------
  //  Lifecycle
  // --------------------------------------------------------------------------------------------

  override def nextRecord(reuse: T): T = {
    checkState(!reachedEnd())
    checkState(currentSplit != null && currentSplit.getStart == 0)
    checkState(stream != null)
    readRecord(reuse, currentSplit.getPath, stream, currentSplit.getLength)
  }

  override def reachedEnd(): Boolean = {
    stream.getPos != 0
  }
}

@SerialVersionUID(1L)
object WholeFileInputFormat {

  @throws[IOException]
  def readFully(fileStream: FSDataInputStream, fileLength: Long): Array[Byte] = {
    if(fileLength > Int.MaxValue) {
      throw new IllegalArgumentException("the file is too large to be fully read")
    }
    val buf = new Array[Byte](fileLength.toInt)
    readFully(fileStream, buf, 0, fileLength.toInt)
    buf
  }

  @throws[IOException]
  def readFully(inputStream: InputStream, buf: Array[Byte], off: Int, len: Int): Array[Byte] = {
    var bytesRead = 0
    while (bytesRead < len) {
      val read = inputStream.read(buf, off + bytesRead, len - bytesRead)
      if (read < 0) throw new EOFException("Premature end of stream")
      bytesRead += read
    }
    buf
  }
}
