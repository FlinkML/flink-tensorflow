package org.apache.flink.contrib.tensorflow.types

import java.nio._

import com.google.protobuf.Message
import com.twitter.bijection.Inversion.attemptWhen
import com.twitter.bijection._
import com.twitter.bijection.protobuf.ProtobufCodec
import org.apache.flink.api.common.typeinfo.{TypeHint, TypeInformation}
import org.apache.flink.contrib.tensorflow.TFUtils
import org.tensorflow.{DataType, Tensor}
import org.apache.flink.api.java.tuple.{Tuple => FlinkTuple}
import org.apache.flink.contrib.tensorflow.types.Rank._
import org.apache.flink.api.scala._

import scala.reflect.ClassTag
import scala.reflect.classTag
import scala.util.Try

/**
  */
trait TensorValueInjections extends ByteStringToTensorValueInjections {
}

trait ByteStringToTensorValueInjections extends Commons {

  implicit def byteString2TensorValue: Injection[ByteString, TensorValue[`0D`, ByteString]] =
    new AbstractInjection[ByteString, TensorValue[`0D`, ByteString]] {
      def apply(arry: ByteString) = TensorValue.fromTensor(Tensor.create(arry))

      override def invert(t: TensorValue[`0D`, ByteString]) = ???
//        attemptWhen(t)(_.dataType() == DataType.STRING)(_.bytesValue())
    }
}
