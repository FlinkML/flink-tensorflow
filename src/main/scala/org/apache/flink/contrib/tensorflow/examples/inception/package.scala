package org.apache.flink.contrib.tensorflow.examples

import org.apache.flink.contrib.tensorflow.types.Rank.`4D`
import org.apache.flink.contrib.tensorflow.types.TensorValue

/**
  * @author Eron Wright
  */
package object inception {

  /**
    * A list of images encoded as a 4-D tensor of floats.
    */
  type ImageTensor = TensorValue[`4D`,Float]
}
