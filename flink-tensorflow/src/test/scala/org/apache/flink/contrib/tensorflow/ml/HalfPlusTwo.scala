package org.apache.flink.contrib.tensorflow.ml

import org.apache.flink.contrib.tensorflow.ml.signatures.RegressionMethod
import org.apache.flink.contrib.tensorflow.models.ModelFunction
import org.apache.flink.contrib.tensorflow.models.savedmodel.TensorFlowModel
import org.apache.flink.contrib.tensorflow.models.savedmodel.TensorFlowModel._
import org.apache.flink.core.fs.Path

/**
  * The half-plus-two linear regression model.
 *
  * @param modelPath path to the saved model data.
  */
@SerialVersionUID(1L)
class HalfPlusTwo(modelPath: Path) extends TensorFlowModel[HalfPlusTwo] {

  override protected val loader = load(modelPath, "serve")

  /**
    * Estimates y for a given x using the half-plus-two model.
    */
  def regress_x_to_y = ModelFunction[RegressionMethod](session(), signatureDef("regress_x_to_y").get)

  /**
    * Estimates y for a given x using the half-plus-two model.
    */
  def regress_x_to_y2 = ModelFunction[RegressionMethod](session(), signatureDef("regress_x_to_y2").get)
}
