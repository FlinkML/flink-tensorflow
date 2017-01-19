package org.apache.flink.contrib.tensorflow.models.savedmodel

import java.nio.file.Paths

import org.apache.flink.core.fs.Path
import org.tensorflow.SavedModelBundle
import org.tensorflow.framework.MetaGraphDef

import scala.collection.JavaConverters._

/**
  * The default saved model loader.
  *
  * <p>Loads models from a distributed filesystem.
  *
  * @param exportPath the path to load from.
  * @param tags       the tags associated with the specific graph to load.
  */
class DefaultSavedModelLoader(exportPath: Path, tags: Set[String])
  extends SavedModelLoader {

  override lazy val metagraph: MetaGraphDef = {
    // TODO(eronwright) load metagraph directly (rather than loading the full bundle)

    // TODO support remote paths
    val localPath = Paths.get(exportPath.toUri)
    val bundle = SavedModelBundle.loadSavedModel(localPath.toString, tags.asJava)
    try {
      MetaGraphDef.parseFrom(bundle.metaGraphDef())
    }
    finally {
      bundle.close()
    }
  }

  override def load(): SavedModelBundle = {
    // TODO(eronwright) support remote paths
    val localPath = Paths.get(exportPath.toUri)
    SavedModelBundle.loadSavedModel(localPath.toString, tags.asJava)
  }
}
