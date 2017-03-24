package example

import com.twitter.bijection._
import com.twitter.bijection.Conversion._
import org.apache.flink.api.java.tuple.{Tuple => FlinkTuple}
import example.ComputeLoss._
import org.apache.flink.contrib.tensorflow.models.savedmodel.TensorFlowModel
import org.tensorflow.contrib.scala.Rank._
import org.apache.flink.contrib.tensorflow.types.TensorInjections._
import org.apache.flink.contrib.tensorflow.types.{TensorRef, TensorValue}
import org.tensorflow.{Session, Tensor}
import org.tensorflow.example.Example
import org.tensorflow.framework.SignatureDef

import resource._

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

    val model: ExampleModel = ???

    val input: ExampleTensor = ???


    val y: ManagedResource[LossTensor] = model.lossA(fromExample(input))


    y.acquireAndGet(_.shape())


//    val lossA: Result[LossTensor] = model.lossA(input1)
//    for(i1 <- input1;
//      lossA <- model.lossA(i1)) {
//      lossA.toTextLabels
//    }

    //    val (lossA, lossB) = model.lossAB(input)
  }
}
