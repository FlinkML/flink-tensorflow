package org.apache.flink.contrib.tensorflow.ml.models

import org.apache.flink.contrib.tensorflow.ml.signatures.RegressionSignature
import org.apache.flink.contrib.tensorflow.models.savedmodel.{DefaultSavedModelLoader, SavedModelLoader, TensorFlowModel}
import org.apache.flink.core.fs.Path

/**
  * The half-plus-two linear regression model.
 *
  * @param modelPath path to the saved model data.
  */
class HalfPlusTwo(modelPath: Path) extends TensorFlowModel[HalfPlusTwo] {
  import org.apache.flink.contrib.tensorflow.models.savedmodel.SignatureConstants._
  import TensorFlowModel._

//  override protected def loader: SavedModelLoader =
//    new DefaultSavedModelLoader(modelPath, Set("serve"))

  override protected def loader = load(modelPath, Set("serve"))

  // supported methods by the half-plus-two model

  implicit val regress: RegressionSignature[HalfPlusTwo] =
    new RegressionSignature(signatureDef(REGRESS_METHOD_NAME).getOrElse(sys.error("missing regress method")))
}
