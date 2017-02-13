package org.apache.flink.contrib.tensorflow.models.savedmodel

import org.apache.flink.contrib.tensorflow.models.Model.RunnableSignature
import org.apache.flink.contrib.tensorflow.models.{Model, RichModel, Signature}
import org.apache.flink.core.fs.Path
import org.apache.flink.util.Preconditions.checkState
import org.tensorflow.framework.{MetaGraphDef, SignatureDef}
import org.tensorflow.{Graph, SavedModelBundle, Session}

/**
  * A TensorFlow Model based on the saved model format.
  */
trait TensorFlowModel[Self <: TensorFlowModel[Self]]
  extends RichModel[Self] {
  that: Self =>

  /**
    * The metagraph associated with the saved model.
    */
  def metagraph: MetaGraphDef = loader.metagraph()

  /**
    * Lookup a signaturedef by name.
    */
  def signatureDef(name: String): Option[SignatureDef] = {
    Option(metagraph.getSignatureDefMap.get(name))
  }

  /**
    * The loader for the underlying saved model.
    */
  protected def loader: SavedModelLoader

  // -- RUNTIME --

  @transient private var bundle: SavedModelBundle = _

  protected def session(): Session = bundle.session()

  override def open() {
    checkState(bundle == null)
    bundle = loader.load()
  }

  protected def run[OUT](runnable: RunnableSignature[OUT]): OUT = {
    checkState(bundle != null)
    val context = new Model.RunContext {
      override def graph: Graph = bundle.graph()
      override def session: Session = bundle.session()
    }
    runnable(context)
  }

  override def close() {
    if (bundle != null) {
      bundle.close()
      bundle = null
    }
  }
}

object TensorFlowModel {
  /**
    * Produces a saved-model loader for the model at the provided location.
    * @param modelPath the saved-model directory
    * @param tags the tags identifying the specific metagraph to load
    * @return a loader
    */
  def load(modelPath: Path, tags: Set[String]): SavedModelLoader =
    new DefaultSavedModelLoader(modelPath, tags)
}
