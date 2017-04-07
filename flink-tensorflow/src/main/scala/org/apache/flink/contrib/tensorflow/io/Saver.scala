package org.apache.flink.contrib.tensorflow.io

import com.twitter.bijection.Conversion._
import org.apache.flink.contrib.tensorflow.types.TensorInjections._
import org.apache.flink.contrib.tensorflow.types.TensorName
import org.tensorflow.util.SaverDef
import org.tensorflow.{Session, Tensor}

import scala.collection.JavaConverters._

/**
  * A saver of session state.
  */
trait Saver {

  /**
    * Saves variables.
    *
    * This method runs the ops added by the builder for saving variables.
    * It requires a session in which the graph was launched.  The variables to
    * save must also have been initialized.
    *
    * The method returns the path of the newly created checkpoint file.  This
    * path can be passed directly to a call to `restore()`.
    *
    * See https://github.com/tensorflow/tensorflow/blob/master/tensorflow/python/training/saver.py#L1284
    * @param sess A `Session` to use to save the parameters.
    * @param savePath Path to save parameters to.
    */
  def save(sess: Session, savePath: String): String

  /**
    * Restores previously saved variables.
    *
    * This method runs the ops added by the builder for restoring variables.
    * It requires a session in which the graph was launched.  The variables to
    * restore do not have to have been initialized, as restoring is itself a way
    * to initialize variables.
    *
    * @param sess A `Session` to use to restore the parameters.
    * @param savePath Path where parameters were previously saved.
    * @return
    */
  def restore(sess: Session, savePath: String): Unit

}

object Saver {
  def apply(saverDef: SaverDef): Saver = new DefaultSaver(saverDef)
}

/**
  * A Saver implementation based on a [[SaverDef]].
  */
class DefaultSaver(saverDef: SaverDef) extends Saver {

  override def save(sess: Session, savePath: String): String = {
    val savePathT = savePath.as[Tensor]
    try {
      val filenameTensorName = TensorName(saverDef.getFilenameTensorName)
      val saveTensorName = TensorName(saverDef.getSaveTensorName)
      val result = sess.runner()
        .fetch(saveTensorName.name, saveTensorName.index)
        .feed(filenameTensorName.name, filenameTensorName.index, savePathT)
        .run().asScala
      try {
        val checkpointPath = new String(result.head.bytesValue())
        checkpointPath
      }
      finally {
        result.foreach(_.close())
      }

    } finally { savePathT.close() }
  }

  override def restore(sess: Session, savePath: String): Unit = {
    val savePathT = savePath.as[Tensor]
    try {
      val filenameTensorName = TensorName(saverDef.getFilenameTensorName)
      val restoreOpName = TensorName(saverDef.getRestoreOpName)
      val result = sess.runner()
        .addTarget(saverDef.getRestoreOpName)
        .feed(filenameTensorName.name, filenameTensorName.index, savePathT)
        .run().asScala
      result.foreach(_.close())
    } finally { savePathT.close() }
  }
}
