package org.apache.flink.contrib.tensorflow.models.savedmodel;

/**
 * See https://github.com/tensorflow/tensorflow/blob/master/tensorflow/cc/saved_model/signature_constants.h
 */
public class SignatureConstants {


	/* Key in the signature def map for `default` serving signatures. The default
	 * signature is used in inference requests where a specific signature was not
	 * specified.
	 */
	public static final String DEFAULT_SERVING_SIGNATURE_DEF_KEY = "serving_default";

	/* ################################################################################
	 * Classification API constants.
	 */

	// Classification inputs.
	public static final String CLASSIFY_INPUTS = "inputs";

	// Classification method name used in a SignatureDef.
	public static final String CLASSIFY_METHOD_NAME = "tensorflow/serving/classify";

	// Classification classes output.
	public static final String CLASSIFY_OUTPUT_CLASSES = "classes";

	// Classification scores output.
	public static final String CLASSIFY_OUTPUT_SCORES = "scores";

	/* ################################################################################
	 * Prediction API constants.
	 */

	// Predict inputs.
	public static final String PREDICT_INPUTS = "inputs";

	// Prediction method name used in a SignatureDef.
	public static final String PREDICT_METHOD_NAME = "tensorflow/serving/predict";

	// Predict outputs.
	public static final String PREDICT_OUTPUTS = "outputs";

	/* ################################################################################
	 * Regression API constants.
	 */

	// Regression inputs.
	public static final String REGRESS_INPUTS = "inputs";

	// Regression method name used in a SignatureDef.
	public static final String REGRESS_METHOD_NAME = "tensorflow/serving/regress";

	// Regression outputs.
	public static final String REGRESS_OUTPUTS = "outputs";

}
