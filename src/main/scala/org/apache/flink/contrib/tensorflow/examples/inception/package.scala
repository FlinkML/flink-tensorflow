package org.apache.flink.contrib.tensorflow.examples

import org.apache.flink.contrib.tensorflow.types.Rank._
import org.apache.flink.contrib.tensorflow.types.TensorInjections.ByteString
import org.apache.flink.contrib.tensorflow.types.TensorValue

package object inception {

  /**
    * A list of images encoded as a 4-D tensor of floats.
    */
  type ImageTensor = TensorValue[`4D`,Float]

  /**
    * An image file.
    */
  type ImageFile = ByteString

  /**
    * A 0-D tensor containing an image file.
    */
  type ImageFileTensor = TensorValue[`0D`, ImageFile]

  /**
    * A set of labels encoded a 2-D tensor of floats.
    */
  type LabelTensor = TensorValue[`2D`,Float]
}
