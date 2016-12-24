package org.apache.flink.contrib.tensorflow.examples.inception;

import org.apache.flink.contrib.tensorflow.examples.GraphBuilder;
import org.apache.flink.contrib.tensorflow.streaming.functions.RichGraphFunction;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tensorflow.DataType;
import org.tensorflow.Output;
import org.tensorflow.Session;
import org.tensorflow.Tensor;
import org.tensorflow.framework.GraphDef;

import java.util.Arrays;
import java.util.List;

/**
 * Normalize a JPEG image.
 * <p>
 * The output is compatible with inception5h.
 */
public class ImageNormalization extends RichGraphFunction<byte[], byte[]> {

	protected static final Logger LOG = LoggerFactory.getLogger(ImageNormalization.class);

	//	private final static byte[][] INPUT_IMAGE_TEMPLATE = new byte[600][800];
	private final static byte[] INPUT_IMAGE_TEMPLATE = new byte[86412];

	private String inputName;
	private String outputName;

	public GraphDef buildGraph() {
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

			GraphDef graphDef = b.build();

			return graphDef;
		}
	}

	@Override
	public void processElement(byte[] value, Context ctx, Collector<byte[]> out) throws Exception {

		// convert the input element
		Tensor inputTensor = Tensor.create(value);

		// define the command to fetch the output tensor
		Session.Runner command = ctx.session().runner()
			.feed(inputName, inputTensor)
			.fetch(outputName);

		// run the command
		List<Tensor> outputTensors = command.run();
		if (outputTensors.size() != 1) {
			throw new IllegalStateException("fetch failed to produce a tensor");
		}
		Tensor outputTensor = outputTensors.get(0);
		float[][][][] outputValue = new float
			[(int) outputTensor.shape()[0]]
			[(int) outputTensor.shape()[1]]
			[(int) outputTensor.shape()[2]]
			[(int) outputTensor.shape()[3]];
		outputTensors.get(0).copyTo(outputValue);
		LOG.info("normalized an image ({})", Arrays.toString(outputTensor.shape()));
	}

	@Override
	public void onTimer(long timestamp, OnTimerContext ctx, Collector<byte[]> out) throws Exception {

	}
}
