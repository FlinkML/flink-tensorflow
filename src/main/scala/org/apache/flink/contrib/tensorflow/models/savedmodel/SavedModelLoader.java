package org.apache.flink.contrib.tensorflow.models.savedmodel;

import org.apache.flink.contrib.tensorflow.models.Model;
import org.tensorflow.SavedModelBundle;
import org.tensorflow.framework.MetaGraphDef;

/**
 * Abstract loader to inspect and load a saved model.
 */
public interface SavedModelLoader {

	/**
	 * Get the metagraph associated with the model.
	 * <p>
	 * <p>The metagraph is considered inexpensive and safe to load,
	 * such as during Flink program definition.
	 */
	MetaGraphDef metagraph();

	/**
	 * Load the saved model bundle.
	 */
	SavedModelBundle load();
}
