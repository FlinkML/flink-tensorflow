package org.apache.flink.contrib.tensorflow.ml.models

import com.twitter.bijection.{@@, Rep}
import org.apache.flink.contrib.tensorflow.models.Model
import org.tensorflow.Tensor
import org.tensorflow.example.Example

/**
  */
abstract class Inception extends Model[Inception] {

  // the inception model is an ad-hoc model without a metagraphdef nor signatures,
  // so we construct adhoc signatures here.   It is also possible to use a standard signature
  // with a adhoc model by synthesizing a signaturedef.

  // load inception model

  def label(t: Tensor @@ Rep[Example]): Tensor @@ Rep[Example] = ???
  def lossA() = ???
  def lossB() = ???
}
