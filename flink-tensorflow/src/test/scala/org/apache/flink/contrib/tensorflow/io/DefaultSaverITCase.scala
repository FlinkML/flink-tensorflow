package org.apache.flink.contrib.tensorflow.io

import org.apache.flink.contrib.tensorflow.models.savedmodel.DefaultSavedModelLoader
import org.apache.flink.contrib.tensorflow.util.{FlinkTestBase, RegistrationUtils}
import org.apache.flink.core.fs.Path
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpecLike}
import org.tensorflow.{Session, Tensor}

import scala.collection.JavaConverters._

@RunWith(classOf[JUnitRunner])
class DefaultSaverITCase extends WordSpecLike
  with Matchers
  with FlinkTestBase {

  override val parallelism = 1

  "A DefaultSaver" should {
    "run the save op" in {
      val env = StreamExecutionEnvironment.getExecutionEnvironment
      RegistrationUtils.registerTypes(env.getConfig)

      val loader = new DefaultSavedModelLoader(new Path("../models/half_plus_two"), "serve")
      val bundle = loader.load()
      val saverDef = loader.metagraph.getSaverDef
      val saver = new DefaultSaver(saverDef)

      def getA = getVariable(bundle.session(), "a").floatValue()
      def setA(value: Float) = setVariable(bundle.session(), "a", Tensor.create(value))

      val initialA = getA
      println("Initial value: " + initialA)

      setA(1.0f)
      val savePath = tempFolder.newFolder("model-0").getAbsolutePath
      val path = saver.save(bundle.session(), savePath)
      val savedA = getA
      savedA shouldBe (1.0f)
      println("Saved value: " + getA)

      setA(2.0f)
      val updatedA = getA
      updatedA shouldBe (2.0f)
      println("Updated value: " + updatedA)

      saver.restore(bundle.session(), path)
      val restoredA = getA
      restoredA shouldBe (savedA)
      println("Restored value: " + restoredA)
    }

    def getVariable(sess: Session, name: String): Tensor = {
      val result = sess.runner().fetch(name).run().asScala
      result.head
    }

    def setVariable(sess: Session, name: String, value: Tensor): Unit = {
      sess.runner()
        .addTarget(s"$name/Assign")
        .feed(s"$name/initial_value", value)
        .run()
    }
  }
}
