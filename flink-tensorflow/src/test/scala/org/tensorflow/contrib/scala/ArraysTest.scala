package org.tensorflow.contrib.scala

import com.twitter.bijection.Conversion._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpecLike}
import org.tensorflow.contrib.scala.Arrays._
import org.tensorflow.contrib.scala.Rank._
import resource._

@RunWith(classOf[JUnitRunner])
class ArraysTest extends WordSpecLike
  with Matchers {

  "Arrays" when {
    "Array[Float]" should {
      "convert to Tensor[`1D`,Float]" in {
        val expected = Array(1f,2f,3f)
        managed(expected.as[TypedTensor[`1D`,Float]]).foreach { t =>
          t.shape shouldEqual Array(expected.length)
          val actual = t.as[Array[Float]]
          actual shouldEqual expected
        }
      }
    }
  }
}
