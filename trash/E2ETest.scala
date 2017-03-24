package org.apache.flink.contrib.tensorflow.experimental

import java.nio.{ByteBuffer, ByteOrder, FloatBuffer, IntBuffer}

import org.junit.runner.RunWith
import com.twitter.bijection.Conversion.asMethod
import com.twitter.bijection._
import org.scalatest.junit.JUnitRunner
import org.tensorflow.example.Example
import org.tensorflow.{DataType, Graph, Session, Tensor}
import org.scalatest.Matchers._
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Ignore, Matchers, WordSpecLike}

import scala.util.{Success, Try}
import resource._
import com.twitter.bijection.Inversion.attemptWhen
import org.apache.flink.api.java.tuple.Tuple
import org.apache.flink.contrib.tensorflow.examples.common.GraphBuilder
import org.apache.flink.contrib.tensorflow.experimental._
import org.apache.flink.contrib.tensorflow.types.TensorValue
import org.tensorflow.Session
import org.tensorflow.Session.Run
import org.tensorflow.contrib.scala._
//import org.apache.flink.contrib.tensorflow._
import E2ETest._
import scala.reflect.{ClassTag, classTag}
import org.tensorflow.contrib.scala.Arrays._
import org.tensorflow.contrib.scala.Tensors._

@RunWith(classOf[JUnitRunner])
@Ignore
class E2ETest
  extends WordSpecLike
  with Matchers {

  "Resource[Tensor]" should {
    "close" in {
      for(t <- managed(Tensor.create(sampleData))) {
        val actual = new Array[Int](sampleData.length)
        t.copyTo(actual)
        actual shouldEqual sampleData
      }
      // tensor shouldBe closed
    }

    "support and" in {
      val input = managed(tensor(sampleData)) and managed(tensor(sampleData))
      input { case (t1,t2) =>
        val actual = new Array[Int](sampleData.length)
        t1.copyTo(actual)
        actual shouldEqual sampleData
      }
      // tensor shouldBe closed
    }
  }

  "Resource[Product]" should {
    "support case classes" in {
      // some number of tensors are stored in the case class
      val signatureOutput = SampleSignatureInput(
        x = Tensor.create(sampleData),
        y = Tensor.create(sampleData))

      for(u <- managed(signatureOutput)) {
        val actual = new Array[Int](sampleData.length)
        u.x.copyTo(actual)
        actual shouldEqual sampleData
        u.y.copyTo(actual)
        actual shouldEqual sampleData
      }

      // all tensors are now closed
      // signatureOutput.x shouldBe closed
      // signatureOutput.y shouldBe closed

    }
  }

  "Resource[Session]" should {
    "close" in {
      val session = new Session(graph)
      for(session <- managed(session)) {
        session.runner()
      }

      // session shouldBe closed
    }
  }

  "Resource[Session.Run]" should {

    val model = new Model()

    "close" in {
      for {
        x <- managed(tensor(sampleData))
        y <- model.regress(x)
      } {
        println(s"regress(x) = $y")
      }
    }

    "support yield" in {
      val output = for {
        x <- managed(tensor(sampleData))
        r <- model.regress(x)
      } yield r
      val value = output.acquireAndGet(TensorValue.fromTensor)
      // tensor shouldBe closed
      println(value)
    }

    "be concise" in {
      val value = managed(tensor(sampleData)).flatMap(model.regress).acquireAndGet(TensorValue.fromTensor)
      // tensor shouldBe closed
      println(value)
    }
  }

  "Tensor @@ Tag" should {
//
//    import TensorTags._
//    import org.apache.flink.contrib.tensorflow.types.Rank._
//
//    object Regression {
//      type Input = Tensor @@ `1D`
//      type Output = Tensor @@ `1D`
//
//      def regress(t: Input): ManagedResource[Output] =
//        managed(t.as[Array[Int]].as[Output])
//
//      def labels(t: Output): List[String] = {
//        List(t.toString)
//      }
//    }
//
//    import Regression._
//
////    "support tagging" in {
////      val t = Tensor.create(sampleData).taggedWith[`1D`]
////      t.close()
////    }
//
//    "tagging helper" in {
//      val t = Tensor.create(sampleData).withRank[`1D`]
//      t.close()
//    }
//
//    "support conversion" in {
//      val tensor = sampleData.as[Tensor @@ `1D`]
//      tensor.close
//    }
//
//    "support conversion (generic)" in {
//      val tensor = sampleDataF.as[Tensor @@ `1D`]
//      tensor.close
//    }
//
//
//    "support managedresource" in {
//      val tensor = sampleData.as[Tensor @@ `1D`]
//
//      for(t <- managed(tensor)) {
//        val actual = t.as[Array[Int]]
//        actual shouldEqual sampleData
//      }
//    }
//
//    "support method composition" in {
//      // below demonstrates composition of graph computations
//      val x = managed(sampleData.as[Input])
//      val y: Array[Int] = x
//        .flatMap(regress)
//        .flatMap(regress)
//        .acquireAndGet(_.as[Array[Int]])
//      println(y.toList)
//    }
//
//    "support labeling use case (with for)" in {
//      for {
//        x <- managed(sampleData.as[Input])
//        y <- regress(x)
//      } {
//        println(labels(y))
//      }
//    }
//
//    "support labeling use case (with map)" in {
//        val x = managed(sampleData.as[Input])
//        val y: List[String] = x.flatMap(regress)(labels)
//        println(y)
//    }
//
////    "support conversion" in {
////      val value = tensor(sampleData)
////        .flatMap(model.regress)
////        .acquireAndGet(r => TensorValue.fromTensor(r.outputs.get(0)))
////
////      // tensor shouldBe closed
////      println(value)
////    }
  }

  "TypedTensor" should {

    import org.tensorflow.contrib.scala.Rank._

    object Regression {
      type Input = TypedTensor[`1D`,Int]
      type Output = TypedTensor[`1D`,Int]

      def regress(t: Input): ManagedResource[Output] =
        managed(t.as[Array[Int]].as[Output])

      def labels(t: Output): List[String] = {
        List(t.toString)
      }
    }

    import Regression._

    //    "support tagging" in {
    //      val t = Tensor.create(sampleData).taggedWith[`1D`]
    //      t.close()
    //    }

    "tagging helper" in {
      val t = Tensor.create(sampleData).taggedWith[`1D`,Int]
      t.close()
    }

    "support conversion" in {
      val tensor = sampleData.as[Input]
      tensor.close
    }

    "support conversion (generic)" in {
      val tensor = sampleDataF.as[TypedTensor[`1D`,Float]]
      tensor.close
    }

    "support managedresource" in {
      val tensor = sampleData.as[Input]

      for(t <- managed(tensor)) {
        val actual = t.as[Array[Int]]
        actual shouldEqual sampleData
      }
    }

    "support method composition" in {
      // below demonstrates composition of graph computations
      val x = managed(sampleData.as[Input])
      val y: Array[Int] = x
        .flatMap(regress)
        .flatMap(regress)
        .acquireAndGet(_.as[Array[Int]])
      println(y.toList)
    }

    "support labeling use case (with for)" in {
      for {
        x <- managed(sampleData.as[Input])
        y <- regress(x)
      } {
        println(labels(y))
      }
    }

    "support labeling use case (with map)" in {
      val x = managed(sampleData.as[Input])
      val y: List[String] = x.flatMap(regress)(labels)
      println(y)
    }

    "support TensorValue" in {
      val x = managed(sampleData.as[Input])
      val t = x.acquireAndGet(_.as[TensorValue[`1D`,Int]])

    }
    //    "support conversion" in {
    //      val value = tensor(sampleData)
    //        .flatMap(model.regress)
    //        .acquireAndGet(r => TensorValue.fromTensor(r.outputs.get(0)))
    //
    //      // tensor shouldBe closed
    //      println(value)
    //    }
  }
}

object E2ETest {
  val sampleData = Array(1,2,3)
  val sampleDataF = Array(1f,2f,3f)

  case class SampleSignatureInput(x: Tensor, y: Tensor)

  def tensor(t: => Any): Tensor = Tensor.create(t)

  def graph: Graph = {
    val b: GraphBuilder = new GraphBuilder
    val x = b.constant("x", sampleData)
    val y = b.constant("y", sampleData)
    val z = b.sub(y, x)
    b.build()
  }

  class Model {
    val g = graph
    val session = new Session(g)

    def regress(x: Tensor): ManagedResource[Tensor] = {
      // the session is not run yet; later the managed resource will be acquired

//      session.runner().feed("x", x).fetch("Sub",0).managed.map(_.outputs.get(0))
      ???
    }

    def close(): Unit = {
      session.close()
      g.close()
    }
  }

  object RegressionMethod {
    case class Input(x: Tensor)
    case class Output(y: Tensor)
  }


//
//
//  implicit object runResource extends Resource[Session.Run] {
//
//    import scala.collection.JavaConverters._
//
//    override def close(r: Run): Unit = r.outputs.asScala.foreach(_.close())
//
//    override def toString: String = "Resource[Session.Run]"
//  }
//
//  def runSession(runner: Session#Runner): ManagedResource[Session.Run] = {
//    def opener: Session.Run = {
//      runner.runAndFetchMetadata()
//    }
//    managed(opener)
//  }
//
//  // fixme: seems wrong for managed state (the runner w/ tensors) to be here
//  class RunManagedResource(runner: Session#Runner) extends AbstractManagedResource[Session.Run] {
//    import scala.collection.JavaConverters._
//
//    override protected def open: Run = runner.runAndFetchMetadata()
//
//    override protected def unsafeClose(handle: Run, errors: Option[Throwable]): Unit =
//      handle.outputs.asScala.foreach(_.close())
//  }
}

