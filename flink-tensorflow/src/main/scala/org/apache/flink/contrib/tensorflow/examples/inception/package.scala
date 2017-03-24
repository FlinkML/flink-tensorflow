package org.apache.flink.contrib.tensorflow.examples

import org.apache.flink.contrib.tensorflow.types.TensorValue
import org.tensorflow.contrib.scala.Rank._
import org.tensorflow.contrib.scala._

package object inception {

  /**
    * A list of images encoded as a 4-D tensor of floats.
    */
  type ImageTensor = TypedTensor[`4D`,Float]

  type ImageTensorValue = TensorValue[`4D`, Float]

  /**
    * An image file (tag).
    */
  trait ImageFile

  /**
    * A 0-D tensor containing an image file.
    */
  type ImageFileTensor = TypedTensor[`0D`, ByteString[ImageFile]]

  /**
    * A set of labels encoded a 2-D tensor of floats.
    */
  type LabelTensor = TypedTensor[`2D`,Float]
}
