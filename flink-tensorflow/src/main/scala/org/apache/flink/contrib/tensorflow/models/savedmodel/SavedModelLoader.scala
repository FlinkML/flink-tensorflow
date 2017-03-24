package org.apache.flink.contrib.tensorflow.models.savedmodel

import org.tensorflow.SavedModelBundle
import org.tensorflow.framework.MetaGraphDef

/**
  * Abstract loader to inspect and load a saved model.
  */
trait SavedModelLoader {
  /**
    * Get the metagraph associated with the model.
    *
    * The metagraph is considered inexpensive and safe to load,
    * such as during Flink program definition.
    */
  def metagraph: MetaGraphDef

  /**
    * Load the saved model bundle.
    */
  def load(): SavedModelBundle
}
