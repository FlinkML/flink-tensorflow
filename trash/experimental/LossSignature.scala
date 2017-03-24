package org.apache.flink.contrib.tensorflow.experimental

import com.twitter.bijection.Conversion._
import com.twitter.bijection._
import org.apache.flink.api.java.tuple.{Tuple => FlinkTuple}
import org.apache.flink.contrib.tensorflow.models.savedmodel.TensorFlowModel
import org.apache.flink.contrib.tensorflow.types.TensorInjections._
import org.apache.flink.contrib.tensorflow.types.{TensorRef, TensorValue}
import org.tensorflow.contrib.scala.Rank._
import org.tensorflow.example.Example
import org.tensorflow.framework.SignatureDef
import org.tensorflow.{Session, Tensor}
import resource._


object Demo1 {

  implicit val stringResource = new Resource[String] {
    override def close(r: String): Unit = ???
  }

  trait GraphSignature {
    type IN
    type OUT
    def inputs(in: IN): String
    def outputs(s: String): OUT
  }

  class GraphComputation[S <: GraphSignature](implicit val method: S) {
    def apply(in: method.IN): ManagedResource[method.OUT] = {
      val s = method.inputs(in)
      managed(s).map(method.outputs)
    }
  }

  trait FooSignature extends GraphSignature {
    override type IN = Int
    override type OUT = Double
  }

  object FooSignature {
    implicit val impl = new FooSignature {
      override def inputs(in: Int): String = in.toString
      override def outputs(s: String): Double = s.toDouble
    }
  }

  class MyModel {
    import FooSignature._
    def x_to_y = new GraphComputation[FooSignature]
  }

  def main(): Unit = {
    val m = new MyModel
    val x = 1
    val y: ManagedResource[Double] = m.x_to_y(x)
    val z = m.x_to_y(1).map(_.toInt).opt.get
  }
}


object Demo2 {

  implicit val doubleResource = new Resource[Double] {
    override def close(r: Double): Unit = ???
  }

  trait GraphMethod[IN,OUT] {
    def inputs(in: IN): String
    def outputs(s: String): OUT
  }

  class GraphComputation[S,IN,OUT](implicit method: GraphMethod[IN,OUT], ev: Resource[OUT]) {

    def apply(in: IN): ManagedResource[OUT] = {
      val s = method.inputs(in)
      managed(method.outputs(s))
    }
  }

  object GraphComputation {
    def apply[S <: Function1[IN,OUT],IN,OUT](implicit method: GraphMethod[IN,OUT], ev: Resource[OUT]): GraphComputation[S,IN,OUT] = {
      new GraphComputation
    }
  }

  type FooSignature = Int => Double

  object FooSignature {
    type IN = Int
    type OUT = Double
    implicit val fooSignature = new GraphMethod[Int,Double] {
      override def inputs(in: Int): String = in.toString
      override def outputs(s: String): Double = s.toDouble
    }
  }

  class MyModel {
    import FooSignature._
    def x_to_y = new GraphComputation[FooSignature,Int,Double]
    def x_to_y2 = GraphComputation[FooSignature,Int,Double]
  }

  def main(): Unit = {
    val m = new MyModel
    val x = 1
    val y: ManagedResource[Double] = m.x_to_y(x)
  }
}



trait ModelMethod {

  /**
    * The method name.
    */
  def name: String

  /**
    * The result type of the method.
    */
  type Result

  /**
    * Gets the input values to feed when the method is invoked.
    */
  def inputs(): Map[String, Tensor]

  /**
    * Gets the result of invoking the method.
    * @param outputs a map of fetched outputs.
    */
  def outputs(outputs: Map[String, Tensor]): Result

}

trait ModelFunction[T <: ModelMethod] {
  /**
    * Apply the model function to the graph.
    *
    * @param method the method associated with the computation, including input values.
    * @return the output values as provided by the method.
    */
  def apply(method: T): ManagedResource[method.Result]
}

object ModelFunction {

  /**
    * A model function based on a [[SignatureDef]].
    *
    * A [[SignatureDef]] binds the function to a specific graph.
    */
  def apply[T <: ModelMethod](session: Session, signatureDef: SignatureDef): ModelFunction[T] = ???
}

/**
  * Acts as a magnet for specific input types.
  */
sealed trait ComputeLoss extends ModelMethod {
  val name = "some/package/compute_loss"

}

object ComputeLoss {
  type ExampleTensor = TensorValue[`1D`,Example]
  type LossTensor = TensorValue[`1D`,Float]

  type ExampleTensor2 = TensorRef[`1D`,Example]

  implicit def fromExample(input: ExampleTensor) =
    new ComputeLoss {
      type Result = LossTensor
      def inputs(): Map[String, Tensor] = Map("input" -> input.toTensor)
      def outputs(o: Map[String, Tensor]): Result = o("loss_output").as[Option[LossTensor]].get
    }

//  implicit def fromExample[T <% ExampleTensor](input: T) =
//    new ComputeLoss {
//      type Result = LossTensor
//      def inputs(): Map[String, Tensor] = Map("input" -> input.toTensor)
//      def outputs(o: Map[String, Tensor]): Result = o("loss_output").as[Option[LossTensor]].get
//    }

//  implicit def fromExample[T <% ExampleTensor](input: T) =
//    new ComputeLoss {
//      type Result = LossTensor
//      def inputs(): Map[String, Tensor] = Map("input" -> input.toTensor)
//      def outputs(o: Map[String, Tensor]): Result = o("loss_output").as[Option[LossTensor]].get
//    }
}

trait ExampleModel extends TensorFlowModel[ExampleModel] {

  /**
    * Computes loss 'A' for a given input.
    */
  def lossA = ModelFunction[ComputeLoss](session(), signatureDef("lossA").get)

  /**
    * Computes loss 'B' for a given input.
    */
  def lossB = ModelFunction[ComputeLoss](session(), signatureDef("lossB").get)

//  def lossAB = lossA ++ lossB
  //  def lossAB: (ExampleTensor) => (LossTensor, LossTensor) = ???
}


class App {

  def main(): Unit = {

    val bij = implicitly[Bijection[Int, String @@ Rep[Int]]]
//
//    val model: ExampleModel = ???
//
//    val input: ExampleTensor = ???
//
//
//    val y: ManagedResource[LossTensor] = model.lossA(fromExample(input))
//
//
//    y.acquireAndGet(_.shape())
//

//    val lossA: Result[LossTensor] = model.lossA(input1)
//    for(i1 <- input1;
//      lossA <- model.lossA(i1)) {
//      lossA.toTextLabels
//    }

    //    val (lossA, lossB) = model.lossAB(input)
  }
}
