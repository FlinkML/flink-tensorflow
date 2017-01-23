package org.apache.flink.contrib.tensorflow.common.functions

import com.twitter.bijection.Conversion
import org.apache.flink.api.common.functions.RichMapFunction

/**
  * Converts an input element to an output element using a conversion from the Bijection library.
  */
@Deprecated
class Converter[IN, OUT](implicit conv: Conversion[IN, OUT]) extends RichMapFunction[IN,OUT] {
  override def map(value: IN): OUT = conv(value)
}
