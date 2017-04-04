package org.apache.flink.contrib.tensorflow.examples.common;

import com.google.protobuf.InvalidProtocolBufferException;
import org.tensorflow.*;
import org.tensorflow.framework.GraphDef;

/**
 * A graph builder.
 */
public class GraphBuilder implements AutoCloseable {
	private Graph g;

	public static GraphBuilder newBuilder() {
		return new GraphBuilder();
	}

	public GraphBuilder() {
		this.g = new Graph();
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

	Output variable(String name, DataType dtype, Shape shape) {
		return g.opBuilder("Variable", name)
			.setAttr("dtype", dtype)
			.setAttr("shape", shape)
			.build()
			.output(0);
	}

	public Output placeholder(String name, DataType dtype, Shape shape) {
		return g.opBuilder("PlaceholderV2", name)
			.setAttr("dtype", dtype)
			.setAttr("shape", shape)
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
}
