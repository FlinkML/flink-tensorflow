package org.apache.flink.contrib.tensorflow.streaming.models

import org.apache.flink.annotation.PublicEvolving
import org.apache.flink.runtime.state.{ManagedInitializationContext, ManagedSnapshotContext}

/**
  *
  * This interface must be implemented by models that have potentially
  * repartitionable state that needs to be checkpointed. Methods from this interface are called upon checkpointing and
  * initialization of state.
  *
  * On {@link #initializeState(ManagedInitializationContext)} the implementing class receives a
  * {@link ManagedInitializationContext} which provides access to the {@link OperatorStateStore} (all) and
  * {@link org.apache.flink.api.common.state.KeyedStateStore} (only for keyed operators). Those allow to register
  * managed operator / keyed  user states. Furthermore, the context provides information whether or the operator was
  * restored.
  *
  *
  * In {@link #snapshotState(ManagedSnapshotContext)} the implementing class must ensure that all operator / keyed state
  * is passed to user states that have been registered during initialization, so that it is visible to the system
  * backends for checkpointing.
  *
  */
@PublicEvolving
trait CheckpointedModel {
  /**
    * This method is called when a snapshot for a checkpoint is requested. This acts as a hook to the model to
    * ensure that all state is exposed by means previously offered through {@link ManagedInitializationContext} when
    * the model was initialized, or offered now by {@link ManagedSnapshotContext} itself.
    *
    * @param context the context for drawing a snapshot of the model
    */
  @throws[Exception]
  def snapshotState(context: ManagedSnapshotContext)

  /**
    * This method is called when a model is initialized, so that the model can set up its state through
    * the provided context. Initialization typically includes registering user states through the state stores
    * that the context offers.
    *
    * @param context the context for initializing the model
    */
  @throws[Exception]
  def initializeState(context: ManagedInitializationContext)

}
