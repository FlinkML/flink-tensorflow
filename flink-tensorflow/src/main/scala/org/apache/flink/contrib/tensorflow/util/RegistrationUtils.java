package org.apache.flink.contrib.tensorflow.util;

import com.twitter.chill.protobuf.ProtobufSerializer;
import org.apache.flink.api.common.ExecutionConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tensorflow.TensorFlow;
import org.tensorflow.example.*;
import org.tensorflow.framework.*;

/**
 * Utility methods for registering serializable types.
 */
public class RegistrationUtils {

	protected static final Logger LOG = LoggerFactory.getLogger(RegistrationUtils.class);

	public static void registerTypes(ExecutionConfig config) {

		LOG.debug("TensorFlow {}", TensorFlow.version());

		// example.proto
		config.registerTypeWithKryoSerializer(Example.class, ProtobufSerializer.class);
		config.registerTypeWithKryoSerializer(SequenceExample.class, ProtobufSerializer.class);

		// feature.proto
		config.registerTypeWithKryoSerializer(Features.class, ProtobufSerializer.class);
		config.registerTypeWithKryoSerializer(Feature.class, ProtobufSerializer.class);
		config.registerTypeWithKryoSerializer(FeatureList.class, ProtobufSerializer.class);
		config.registerTypeWithKryoSerializer(FeatureLists.class, ProtobufSerializer.class);
		config.registerTypeWithKryoSerializer(FloatList.class, ProtobufSerializer.class);
		config.registerTypeWithKryoSerializer(BytesList.class, ProtobufSerializer.class);
		config.registerTypeWithKryoSerializer(Int64List.class, ProtobufSerializer.class);

		// others
		config.registerTypeWithKryoSerializer(VersionDef.class, ProtobufSerializer.class);
		config.registerTypeWithKryoSerializer(MemoryLogTensorDeallocation.class, ProtobufSerializer.class);
		config.registerTypeWithKryoSerializer(KernelDef.AttrConstraint.class, ProtobufSerializer.class);
		config.registerTypeWithKryoSerializer(FunctionDef.class, ProtobufSerializer.class);
		config.registerTypeWithKryoSerializer(VariableDef.class, ProtobufSerializer.class);
		config.registerTypeWithKryoSerializer(MemoryLogRawDeallocation.class, ProtobufSerializer.class);
		config.registerTypeWithKryoSerializer(NodeOutput.class, ProtobufSerializer.class);
		config.registerTypeWithKryoSerializer(StepStats.class, ProtobufSerializer.class);
		config.registerTypeWithKryoSerializer(NodeExecStats.class, ProtobufSerializer.class);
		config.registerTypeWithKryoSerializer(GradientDef.class, ProtobufSerializer.class);
		config.registerTypeWithKryoSerializer(ResourceHandle.class, ProtobufSerializer.class);
		config.registerTypeWithKryoSerializer(NodeDef.class, ProtobufSerializer.class);
		config.registerTypeWithKryoSerializer(HistogramProto.class, ProtobufSerializer.class);
		config.registerTypeWithKryoSerializer(OpDef.class, ProtobufSerializer.class);
		config.registerTypeWithKryoSerializer(CostGraphDef.Node.InputInfo.class, ProtobufSerializer.class);
		config.registerTypeWithKryoSerializer(NameAttrList.class, ProtobufSerializer.class);
		config.registerTypeWithKryoSerializer(OpDeprecation.class, ProtobufSerializer.class);
		config.registerTypeWithKryoSerializer(KernelDef.class, ProtobufSerializer.class);
		config.registerTypeWithKryoSerializer(OpDef.ArgDef.class, ProtobufSerializer.class);
		config.registerTypeWithKryoSerializer(Summary.Image.class, ProtobufSerializer.class);
		config.registerTypeWithKryoSerializer(AttrValue.ListValue.class, ProtobufSerializer.class);
		config.registerTypeWithKryoSerializer(AllocatorMemoryUsed.class, ProtobufSerializer.class);
		config.registerTypeWithKryoSerializer(OpDef.AttrDef.class, ProtobufSerializer.class);
		config.registerTypeWithKryoSerializer(TensorSliceProto.class, ProtobufSerializer.class);
		config.registerTypeWithKryoSerializer(Summary.Audio.class, ProtobufSerializer.class);
		config.registerTypeWithKryoSerializer(TensorDescription.class, ProtobufSerializer.class);
		config.registerTypeWithKryoSerializer(DeviceLocality.class, ProtobufSerializer.class);
		config.registerTypeWithKryoSerializer(AllocationDescription.class, ProtobufSerializer.class);
		config.registerTypeWithKryoSerializer(FunctionDefLibrary.class, ProtobufSerializer.class);
		config.registerTypeWithKryoSerializer(TensorShapeProto.Dim.class, ProtobufSerializer.class);
		config.registerTypeWithKryoSerializer(Summary.class, ProtobufSerializer.class);
		config.registerTypeWithKryoSerializer(MemoryLogStep.class, ProtobufSerializer.class);
		config.registerTypeWithKryoSerializer(TensorProto.class, ProtobufSerializer.class);
		config.registerTypeWithKryoSerializer(TensorShapeProto.class, ProtobufSerializer.class);
		config.registerTypeWithKryoSerializer(MemoryLogRawAllocation.class, ProtobufSerializer.class);
		config.registerTypeWithKryoSerializer(SummaryDescription.class, ProtobufSerializer.class);
		config.registerTypeWithKryoSerializer(Summary.Value.class, ProtobufSerializer.class);
//		config.registerTypeWithKryoSerializer(FunctionDef.Node.class, ProtobufSerializer.class);
		config.registerTypeWithKryoSerializer(DeviceStepStats.class, ProtobufSerializer.class);
		config.registerTypeWithKryoSerializer(MemoryLogTensorOutput.class, ProtobufSerializer.class);
		config.registerTypeWithKryoSerializer(AttrValue.class, ProtobufSerializer.class);
		config.registerTypeWithKryoSerializer(OpList.class, ProtobufSerializer.class);
		config.registerTypeWithKryoSerializer(DeviceAttributes.class, ProtobufSerializer.class);
		config.registerTypeWithKryoSerializer(SaveSliceInfoDef.class, ProtobufSerializer.class);
		config.registerTypeWithKryoSerializer(GraphDef.class, ProtobufSerializer.class);
		config.registerTypeWithKryoSerializer(CostGraphDef.Node.class, ProtobufSerializer.class);
		config.registerTypeWithKryoSerializer(CostGraphDef.class, ProtobufSerializer.class);
		config.registerTypeWithKryoSerializer(MemoryLogTensorAllocation.class, ProtobufSerializer.class);
		config.registerTypeWithKryoSerializer(CostGraphDef.Node.OutputInfo.class, ProtobufSerializer.class);
		config.registerTypeWithKryoSerializer(TensorSliceProto.Extent.class, ProtobufSerializer.class);
	}
}
