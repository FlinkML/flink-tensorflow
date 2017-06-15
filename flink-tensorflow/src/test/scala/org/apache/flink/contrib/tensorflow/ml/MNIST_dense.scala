package org.apache.flink.contrib.tensorflow.ml

import org.apache.flink.contrib.tensorflow.ml.signatures.PredictionMethod
import org.apache.flink.contrib.tensorflow.models.ModelFunction
import org.apache.flink.contrib.tensorflow.models.savedmodel.TensorFlowModel
import org.apache.flink.contrib.tensorflow.models.savedmodel.TensorFlowModel._
import org.apache.flink.core.fs.Path

/**
  * The MNIST_dense model.
 *
  * @param modelPath path to the saved model data.
  */
@SerialVersionUID(1L)
class MNIST_dense(modelPath: Path) extends TensorFlowModel[MNIST_dense] {

  override protected val loader = load(modelPath, "serve")

  /**
    * Prediction returns scores for all classes
    */
  def predict = ModelFunction[PredictionMethod](session(), signatureDef("predict_images").get)
}
