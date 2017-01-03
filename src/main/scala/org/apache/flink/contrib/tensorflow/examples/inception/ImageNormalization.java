package org.apache.flink.contrib.tensorflow.examples.inception;

import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.contrib.tensorflow.common.TensorValue;
import org.apache.flink.contrib.tensorflow.examples.common.GraphBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tensorflow.*;

import java.util.List;

/**
 * Decodes and normalizes a JPEG image (as a byte[]) as a 4D tensor.
 *
 * <p>The output is compatible with inception5h.
 */
public class ImageNormalization extends RichMapFunction<Tuple2<String, byte[]>, Tuple2<String, TensorValue>>
{
	protected static final Logger LOG = LoggerFactory.getLogger(ImageNormalization.class);

	private transient Session session;

	// the image processing graph
	private transient Graph graph;
	private transient String inputName;
	private transient String outputName;

	//	private final static byte[][] INPUT_IMAGE_TEMPLATE = new byte[600][800];
	private final static byte[] INPUT_IMAGE_TEMPLATE = new byte[86412];

	@Override
	public void open(Configuration parameters) throws Exception {
		super.open(parameters);
		try {
			openGraph();

			session = new Session(graph);
		}
		catch(RuntimeException e) {
			if(session != null) {
				session.close();
				session = null;
			}
			if(graph != null) {
				graph.close();
				graph = null;
			}
			throw e;
		}
	}

	private void openGraph() {
		try (GraphBuilder b = new GraphBuilder()) {
			// Some constants specific to the pre-trained model at:
			// https://storage.googleapis.com/download.tensorflow.org/models/inception5h.zip
			//
			// - The model was trained with images scaled to 224x224 pixels.
			// - The colors, represented as R, G, B in 1-byte each were converted to
			//   float using (value - Mean)/Scale.
			final int H = 224;
			final int W = 224;
			final float mean = 117f;
			final float scale = 1f;

			// Since the graph is being constructed once per execution here, we can use a constant for the
			// input image. If the graph were to be re-used for multiple input images, a placeholder would
			// have been more appropriate.
			final Output input = b.constant("input", INPUT_IMAGE_TEMPLATE);
			inputName = input.op().name();

			final Output output =
				b.div(
					b.sub(
						b.resizeBilinear(
							b.expandDims(
								b.cast(b.decodeJpeg(input, 3), DataType.FLOAT),
								b.constant("make_batch", 0)),
							b.constant("size", new int[]{H, W})),
						b.constant("mean", mean)),
					b.constant("scale", scale));

			outputName = output.op().name();
			graph = b.build();
		}
	}

	@Override
	public void close() throws Exception {
		session.close();
		graph.close();
		super.close();
	}

	@Override
	public Tuple2<String,TensorValue> map(Tuple2<String,byte[]> value) throws Exception {
		TensorValue outputTensor = processImage(value.f1);
		LOG.info("ImageNormalization({}) => {}", value.f0, outputTensor);
		return new Tuple2<>(value.f0, outputTensor);
	}

	private TensorValue processImage(byte[] imageBytes) {
		// convert the input element to a tensor
		Tensor input = Tensor.create(imageBytes);

		// define the command to fetch the output tensor
		Session.Runner command = session.runner()
			.feed(inputName, input)
			.fetch(outputName);

		// run the command
		List<Tensor> outputTensors = command.run();
		if (outputTensors.size() != 1) {
			throw new IllegalStateException("fetch failed to produce a tensor");
		}
		return TensorValue.fromTensor(outputTensors.get(0));
	}
}
