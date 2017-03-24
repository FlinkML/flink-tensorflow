package org.apache.flink.contrib.tensorflow.util

import org.apache.flink.runtime.minicluster.LocalFlinkMiniCluster
import org.apache.flink.streaming.util.TestStreamEnvironment
import org.apache.flink.test.util.TestBaseUtils
import org.junit.rules.TemporaryFolder
import org.scalatest.{BeforeAndAfter, Suite}

// Copied from Apache Flink.

/** Mixin to start and stop a LocalFlinkMiniCluster automatically for Scala based tests.
  * Additionally a TestEnvironment with the started cluster is created and set as the default
  * [[org.apache.flink.api.java.ExecutionEnvironment]].
  *
  * This mixin starts a LocalFlinkMiniCluster with one TaskManager and a number of slots given
  * by parallelism. This value can be overridden in a sub class in order to start the cluster
  * with a different number of slots.
  *
  * The cluster is started once before starting the tests and is re-used for the individual tests.
  * After all tests have been executed, the cluster is shutdown.
  *
  * The cluster is used by obtaining the default [[org.apache.flink.api.java.ExecutionEnvironment]].
  *
  * @example
  *          {{{
  *            def testSomething: Unit = {
  *             // Obtain TestEnvironment with started LocalFlinkMiniCluster
  *             val env = ExecutionEnvironment.getExecutionEnvironment
  *
  *             env.fromCollection(...)
  *
  *             env.execute
  *            }
  *          }}}
  *
  */
trait FlinkTestBase extends BeforeAndAfter {
  that: Suite =>

  var cluster: Option[LocalFlinkMiniCluster] = None
  val parallelism = 4

  protected val tempFolder = new TemporaryFolder()

  before {
    tempFolder.create()
    val cl = TestBaseUtils.startCluster(
      1,
      parallelism,
      false,
      false,
      true)

    TestStreamEnvironment.setAsContext(cl, parallelism)

    cluster = Some(cl)
  }

  after {
    TestStreamEnvironment.unsetAsContext()
    cluster.foreach(c => TestBaseUtils.stopCluster(c, TestBaseUtils.DEFAULT_TIMEOUT))
    tempFolder.delete()
  }

}
