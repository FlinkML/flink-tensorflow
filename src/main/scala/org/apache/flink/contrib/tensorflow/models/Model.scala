package org.apache.flink.contrib.tensorflow.models

import java.io.Serializable

import org.tensorflow.{Graph, Session}

/**
  * Represents a TensorFlow model.
  *
  * A model is a self-contained, hermetic graph with associated assets
  * and well-defined run methods.
  *
  * A model encapsulates state (a graph) and the means to persist it (checkpointing).
  *
  * @tparam Self the Model type.
  */
trait Model[Self] extends Serializable {
  that: Self =>

  /**
    * Apply the given method to the model.
    *
    * @param method the method to apply.
    * @return the result of applying the method.
    */
  def run[IN](input: IN)(implicit method: Signature[Self, IN]): method.OUT
}

object Model {

  /**
    * A context for model methods.
    */
  trait RunContext {

    def graph: Graph

    /**
      * Get the session containing the model graph.
      */
    def session: Session
  }

}

/**
  * A base interface for all rich user-defined models.  This class defines methods for
  * the life cycle of the models, as well as methods to access the context in which the models
  * are executed.
  */
trait RichModel[Self <: RichModel[Self]] extends Model[Self] {
  that: Self =>

  /**
    * Initialization method for the model.  It is called before the run method
    * and thus suitable for one-time initialization work, such as loading a graph
    * and opening a TensorFlow session.
    *
    * @throws Exception Implementations may forward exceptions, which are caught by the runtime.
    */
  @throws(classOf[Exception])
  def open()

  /**
    * Tear-down method for the model.  It is called after the last call to the run method.
    *
    * @throws Exception Implementations may forward exceptions, which are caught by the runtime.
    */
  @throws(classOf[Exception])
  def close()
}
