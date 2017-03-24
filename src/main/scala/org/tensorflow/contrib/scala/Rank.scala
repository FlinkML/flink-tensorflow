package org.tensorflow.contrib.scala

/**
  * A typeclass representing rank in the TensorFlow system.
  */
trait Rank[R] {
  def rank(t: R): Int
}

object Rank {
  implicit def flinkTuple[R <: org.apache.flink.api.java.tuple.Tuple] = new Rank[R] {
    override def rank(r: R): Int = r.getArity
  }

  implicit def scalaType[R <: Product] = new Rank[R] {
    override def rank(r: R): Int = r.productArity
  }

  type `0D`  = org.apache.flink.api.java.tuple.Tuple0
  type `1D`  = org.apache.flink.api.java.tuple.Tuple1[Long]
  type `2D`  = org.apache.flink.api.java.tuple.Tuple2[Long, Long]
  type `3D`  = org.apache.flink.api.java.tuple.Tuple3[Long, Long, Long]
  type `4D`  = org.apache.flink.api.java.tuple.Tuple4[Long, Long, Long, Long]
  type `5D`  = org.apache.flink.api.java.tuple.Tuple5[Long, Long, Long, Long, Long]
  type `6D`  = org.apache.flink.api.java.tuple.Tuple6[Long, Long, Long, Long, Long, Long]
  type `7D`  = org.apache.flink.api.java.tuple.Tuple7[Long, Long, Long, Long, Long, Long, Long]
  type `8D`  = org.apache.flink.api.java.tuple.Tuple8[Long, Long, Long, Long, Long, Long, Long, Long]
  type `9D`  = org.apache.flink.api.java.tuple.Tuple9[Long, Long, Long, Long, Long, Long, Long, Long, Long]
  type `10D` = org.apache.flink.api.java.tuple.Tuple10[Long, Long, Long, Long, Long, Long, Long, Long, Long, Long]
  type `11D` = org.apache.flink.api.java.tuple.Tuple11[Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long]
  type `12D` = org.apache.flink.api.java.tuple.Tuple12[Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long]
  type `13D` = org.apache.flink.api.java.tuple.Tuple13[Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long]
  type `14D` = org.apache.flink.api.java.tuple.Tuple14[Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long]
  type `15D` = org.apache.flink.api.java.tuple.Tuple15[Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long]
  type `16D` = org.apache.flink.api.java.tuple.Tuple16[Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long]
  type `17D` = org.apache.flink.api.java.tuple.Tuple17[Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long]
  type `18D` = org.apache.flink.api.java.tuple.Tuple18[Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long]
  type `19D` = org.apache.flink.api.java.tuple.Tuple19[Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long]
  type `20D` = org.apache.flink.api.java.tuple.Tuple20[Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long]
  type `21D` = org.apache.flink.api.java.tuple.Tuple21[Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long]
  type `22D` = org.apache.flink.api.java.tuple.Tuple22[Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long]
  type `23D` = org.apache.flink.api.java.tuple.Tuple23[Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long]
  type `24D` = org.apache.flink.api.java.tuple.Tuple24[Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long]
  type `25D` = org.apache.flink.api.java.tuple.Tuple25[Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long, Long]
}
