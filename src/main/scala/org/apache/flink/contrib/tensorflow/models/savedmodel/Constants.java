package org.apache.flink.contrib.tensorflow.models.savedmodel;

/**
 * See https://github.com/tensorflow/tensorflow/blob/master/tensorflow/python/saved_model/constants.py
 */
@Deprecated
public class Constants {

	public static final String ASSETS_DIRECTORY = "assets";
	public static final String ASSETS_KEY = "saved_model_assets";
	public static final String MAIN_OP_KEY = "saved_model_main_op";
	public static final String LEGACY_INIT_OP_KEY = "legacy_init_op";
	public static final int SAVED_MODEL_SCHEMA_VERSION = 1;
	public static final String SAVED_MODEL_FILENAME_PB = "saved_model.pb";
	public static final String SAVED_MODEL_FILENAME_PBTXT = "saved_model.pbtxt";
	public static final String VARIABLES_DIRECTORY = "variables";
	public static final String VARIABLES_FILENAME = "variables";

	private Constants() {
	}
}
