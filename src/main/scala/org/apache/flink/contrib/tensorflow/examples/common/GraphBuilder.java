package org.apache.flink.contrib.tensorflow.examples.common;

import com.google.protobuf.InvalidProtocolBufferException;
import org.apache.flink.contrib.tensorflow.streaming.functions.GraphInitializer;
import org.tensorflow.DataType;
import org.tensorflow.Graph;
import org.tensorflow.Output;
import org.tensorflow.Tensor;
import org.tensorflow.framework.GraphDef;
import org.tensorflow.framework.TensorShapeProto;

/**
 */
public class GraphBuilder implements AutoCloseable {
	private Graph g;

	public static GraphBuilder newBuilder() {
		return new GraphBuilder();
	}

	public GraphBuilder() {
		this.g = new Graph();
	}

	public GraphBuilder(Graph graph) {
		this.g = graph;
	}

	public Output div(Output x, Output y) {
		return binaryOp("Div", x, y);
	}

	public Output sub(Output x, Output y) {
		return binaryOp("Sub", x, y);
	}

	public Output resizeBilinear(Output images, Output size) {
		return binaryOp("ResizeBilinear", images, size);
	}

	public Output expandDims(Output input, Output dim) {
		return binaryOp("ExpandDims", input, dim);
	}

	public Output cast(Output value, DataType dtype) {
		return g.opBuilder("Cast", "Cast").addInput(value).setAttr("DstT", dtype).build().output(0);
	}

	public Output decodeJpeg(Output contents, long channels) {
		return g.opBuilder("DecodeJpeg", "DecodeJpeg")
			.addInput(contents)
			.setAttr("channels", channels)
			.build()
			.output(0);
	}

	public Output constant(String name, Object value) {
		try(Tensor t = Tensor.create(value)) {
			return g.opBuilder("Const", name)
				.setAttr("dtype", t.dataType())
				.setAttr("value", t)
				.build()
				.output(0);
		}
	}

	Output variable(String name, DataType dtype, TensorShapeProto shape) {
		return g.opBuilder("Variable", name)
			.setAttr("dtype", dtype)
//			.setAttr("shape", shape)
			.build()
			.output(0);
	}

	public Output placeholder(String name, DataType dtype, TensorShapeProto shape) {
		/**
		 REGISTER_OP("PlaceholderV2")
		 .Output("output: dtype")
		 .Attr("dtype: type")
		 .Attr("shape: shape")
		 .SetShapeFn([](InferenceContext* c) {
		 TensorShapeProto shape;
		 TF_RETURN_IF_ERROR(c->GetAttr("shape", &shape));
		 ShapeHandle output;
		 TF_RETURN_IF_ERROR(c->MakeShapeFromShapeProto(shape, &output));
		 c->set_output(0, output);
		 return Status::OK();
		 })
		 .Doc(R"doc(
		 A placeholder op for a value that will be fed into the computation.

		 N.B. This operation will fail with an error if it is executed. It is
		 intended as a way to represent a value that will always be fed, and to
		 provide attrs that enable the fed value to be checked at runtime.

		 output: A placeholder tensor that must be replaced using the feed mechanism.
		 dtype: The type of elements in the tensor.
		 shape: The shape of the tensor. The shape can be any partially-specified
		 shape.  To be unconstrained, pass in a shape with unknown rank.
		 )doc");
		 */
		return g.opBuilder("PlaceholderV2", name)
			.setAttr("dtype", dtype)
//			.setAttr("shape", shape)
			.build()
			.output(0);
	}

	private Output binaryOp(String type, Output in1, Output in2) {
		return g.opBuilder(type, type).addInput(in1).addInput(in2).build().output(0);
	}

	public Graph build() {
		Graph built = g;
		g = null;
		return built;
	}

	public GraphDef buildGraphDef() {
		try {
			GraphDef def = GraphDef.parseFrom(g.toGraphDef());
			return def;
		} catch (InvalidProtocolBufferException e) {
			throw new RuntimeException("unable to parse graphdef from libtensorflow", e);
		}
	}

	@Override
	public void close() {
		if(g !=null) {
			g.close();
		}
	}

	public static Graph fromGraphDef(GraphDef graphDef, String prefix) {
		Graph g = new Graph();
		g.importGraphDef(graphDef.toByteArray(), prefix);
		return g;
	}
}
