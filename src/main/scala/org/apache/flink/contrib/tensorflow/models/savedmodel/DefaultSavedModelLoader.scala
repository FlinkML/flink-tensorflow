package org.apache.flink.contrib.tensorflow.models.savedmodel

import java.nio.file.Paths

import org.apache.flink.core.fs.{FileSystem, Path}
import org.tensorflow.SavedModelBundle
import org.tensorflow.framework.MetaGraphDef

import DefaultSavedModelLoader._

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

    // TODO(eronwright) support remote paths
    val localPath = Paths.get(resolve(exportPath).toUri)
    val bundle = SavedModelBundle.load(localPath.toString, tags:_*)
    try {
      MetaGraphDef.parseFrom(bundle.metaGraphDef())
    }
    finally {
      bundle.close()
    }
  }

  override def load(): SavedModelBundle = {
    // TODO(eronwright) support remote paths
    val localPath = Paths.get(resolve(exportPath).toUri)
    SavedModelBundle.load(localPath.toString, tags:_*)
  }
}

object DefaultSavedModelLoader {
  private[tensorflow] def resolve(path: Path): Path = {
    new Path(FileSystem.getLocalFileSystem.getWorkingDirectory, path)
  }
}
