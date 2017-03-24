package org.tensorflow.contrib.scala

import org.tensorflow.Session
import resource._

/**
  * Support for TensorFlow sessions.
  */
object Sessions {

  /**
    * Type class to treat a [[Session]] instance as a managed resource.
    */
  implicit object sessionResource extends Resource[Session] {
    override def close(r: Session): Unit = r.close()
    override def toString: String = "Resource[Session]"
  }
}

