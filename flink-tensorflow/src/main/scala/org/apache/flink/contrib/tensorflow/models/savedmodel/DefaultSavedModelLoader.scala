package org.apache.flink.contrib.tensorflow.models.savedmodel

import java.nio.file.{Files, Paths, Path => NioPath}

import org.apache.flink.core.fs.{FileSystem, Path}
import org.apache.flink.runtime.filecache.FileCache
import org.tensorflow.SavedModelBundle
import org.tensorflow.framework.MetaGraphDef

/**
  * The default saved model loader.
  *
  * <p>Loads models from a distributed filesystem.
  *
  * @param exportPath the path to load from.
  * @param tags       the tags associated with the specific graph to load.
  */
@SerialVersionUID(1L)
class DefaultSavedModelLoader(exportPath: Path, tags: String*)
  extends SavedModelLoader with Serializable {

  override lazy val metagraph: MetaGraphDef = {
    // TODO(eronwright) load metagraph directly (rather than loading the full bundle)
    val bundle = DefaultSavedModelLoader.load(exportPath, true, tags:_*)
    try {
      MetaGraphDef.parseFrom(bundle.metaGraphDef())
    }
    finally {
      bundle.close()
    }
  }

  override def load(): SavedModelBundle = {
    DefaultSavedModelLoader.load(exportPath, true, tags:_*)
  }
}

object DefaultSavedModelLoader {

  private[tensorflow] def load(exportPath: Path, optimize: Boolean, tags: String*): SavedModelBundle = {
    val remotePath = resolve(exportPath)
    val localPath: NioPath = remotePath.toUri match {
      case uri if optimize && uri.getScheme == "file" =>
        // optimization: use local path directly
        Paths.get(uri)
      case _ =>
        // copy the model from the distributed fs to the local fs
        val tempDir = Files.createTempDirectory("model-")
        Files.deleteIfExists(tempDir)
        val tempPath = new Path(tempDir.toUri)
        copy(remotePath, tempPath)
        deleteOnExit(tempPath)
        tempDir
    }
    SavedModelBundle.load(localPath.toString, tags:_*)
  }

  private[tensorflow] def resolve(path: Path): Path = {
    path.makeQualified(FileSystem.get(path.toUri))
  }

  private[tensorflow] def copy(sourcePath: Path, targetPath: Path): Unit = {
    FileCache.copy(sourcePath, targetPath, false)
  }

  private[tensorflow] def deleteOnExit(path: Path): Unit = {
    sys.ShutdownHookThread {
      path.getFileSystem.delete(path, true)
    }
  }
}
