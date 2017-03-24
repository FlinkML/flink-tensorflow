package org.apache.flink.contrib.tensorflow.types

import java.nio.{ByteBuffer, ByteOrder}

import com.twitter.bijection.Conversion.asMethod
import com.twitter.bijection._
import org.apache.flink.contrib.tensorflow.util.TestData
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpecLike}
import org.tensorflow.example.Example
import org.tensorflow.{DataType, Tensor}

import scala.util.Success

@RunWith(classOf[JUnitRunner])
class TensorInjectionsTest
  extends WordSpecLike
  with Matchers {

  import TensorInjections._

  "TensorInjections" should {
    "support protobuf messages" which {
      "have syntax variants" in {
        val expected: Example = TestData.examples().head
        val actual1: Tensor = Injection.apply[Example,Tensor](expected)
        val actual2: Tensor = expected.as[Tensor]
        val actual3 = expected.as[Tensor @@ Rep[Example]]

        val inverse1: Example = Injection.apply[Tensor @@ Rep[Example], Example](actual3)
      }

      "convert to Tensor" in {
        val expected: Example = TestData.examples().head
        val converted: Tensor = expected.as[Tensor] //Injection.apply[Example,Tensor](expected)
        try {
          converted shouldBe a [Tensor]
          val actual = Injection.invert[Example, Tensor](converted)
          actual shouldBe a [Success[_]]
          actual.get shouldEqual expected
        }
        finally {
          converted.close()
        }
      }

      "convert to Tuple containing a Tensor" in {
        val expected: (String,Example) = ("example1", TestData.examples().head)
        val converted = expected.as[(String,Tensor @@ Rep[Example])]
        try {
          println(converted)
          val actual = converted.as[(String, Example)]
          println(actual)
          actual shouldEqual expected
        }
        finally {
          converted._2.close()
        }
      }

      "convert a List containing a Tensor" in {
        val expected: Seq[Example] = TestData.examples()
        val converted = expected.as[Seq[Tensor @@ Rep[Example]]]
        println(converted)

        val t: Tensor = converted.head
        println(t)
      }
    }

    "support STRING vectors" in {
      val examples: List[Example] = TestData.examples().toList

      // a hack to write a STRING tensor with numerous values
      val bb = ByteBuffer.allocate(10000).order(ByteOrder.nativeOrder())
      bb.position(examples.size * 8)
      for(i <- examples.indices) {
        val data = examples(i).as[Tensor]
        val b = ByteBuffer.allocate(data.numBytes())
        data.writeTo(b)
        data.close()
        b.flip().position(8)
        bb.putLong(i * 8, bb.position())
        bb.put(b)
      }

      val tensor = Tensor.create(DataType.STRING, Array(examples.size.toLong), bb)
      println(tensor)
    }

//    "support Input/Output typeclasses" which {
//      "convert" in {
//        val regress: RegressionMethod = new RegressionMethod {
//          override def regress(input: Tensor @@ Rep[Example]): Tensor @@ Rep[Example] = {
//            input
//          }
//        }
//
//        val input: Example = TestData.examples().head
//        val output: Example = regress.regress(input.as[Tensor @@ Rep[Example]]).as[Example]
//
//
////        regress.regress()
//      }
//    }
  }
}
