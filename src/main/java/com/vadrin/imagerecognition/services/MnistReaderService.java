package com.vadrin.imagerecognition.services;

import static java.lang.String.format;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.vadrin.neuralnetwork.models.DataSet;

@Service
public class MnistReaderService {
	private static final int LABEL_FILE_MAGIC_NUMBER = 2049;
	private static final int IMAGE_FILE_MAGIC_NUMBER = 2051;

	private static final String TRAINING_IMAGES_FILE_NAME = "static/train-images.idx3-ubyte";
	private static final String TRAINING_LABLES_FILE_NAME = "static/train-labels.idx1-ubyte";
	private static final String TEST_IMAGES_FILE_NAME = "static/t10k-images.idx3-ubyte";
	private static final String TEST_LABLES_FILE_NAME = "static/t10k-labels.idx1-ubyte";

	// TODO: Since MNIST is 28 by 28 images. This method is hardcoded to 28*28
	// double
	private DataSet createSet(InputStream imageInputStream, InputStream lableInputStream) {
		DataSet set = new DataSet();
		try {
			int[] labels = getLabels(lableInputStream);
			List<int[][]> images = getImages(imageInputStream);
			for (int i = 0; i < labels.length; i++) {
				double[] input = new double[28 * 28];
				double[] output = new double[10];
				output[labels[i]] = 1d;
				for (int j = 0; j < 28; j++) {
					for (int k = 0; k < 28; k++) {
						input[k + j * 28] = (double) images.get(i)[j][k];
					}
				}
				set.add(input, output);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return set;
	}

	public DataSet getTrainingSet() throws IOException {
		return createSet(new ClassPathResource(TRAINING_IMAGES_FILE_NAME).getInputStream(),
				new ClassPathResource(TRAINING_LABLES_FILE_NAME).getInputStream());
	}

	// TODO: This method is only created for heroku environment. Since its unable to
	// load all the training date.
	// Ideally you should use the method getTrainingSet().
	public DataSet getSmallTrainingSet() throws IOException {
		DataSet set = new DataSet();
		try {
			InputStream trainingStreamImage = (new ClassPathResource(TRAINING_IMAGES_FILE_NAME)).getInputStream();
			InputStream trainingStreamLable = (new ClassPathResource(TRAINING_LABLES_FILE_NAME)).getInputStream();
			int[] labels = getLabels(trainingStreamLable);
			//List<int[][]> images = getImages(trainingStreamImage);
			ByteBuffer bb = loadStreamToByteBuffer(trainingStreamImage);
			assertMagicNumber(IMAGE_FILE_MAGIC_NUMBER, bb.getInt());
			int numImages = bb.getInt();
			numImages = 20000;
			int numRows = bb.getInt();
			int numColumns = bb.getInt();
			List<int[][]> images = new ArrayList<>();
			for (int i = 0; i < numImages; i++)
				images.add(readImage(numRows, numColumns, bb));
			for (int i = 0; i < images.size(); i++) {
				double[] input = new double[28 * 28];
				double[] output = new double[10];
				output[labels[i]] = 1d;
				for (int j = 0; j < 28; j++) {
					for (int k = 0; k < 28; k++) {
						input[k + j * 28] = (double) images.get(i)[j][k];
					}
				}
				set.add(input, output);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return set;
	}

	public DataSet getTestSet() throws IOException {
		return createSet(new ClassPathResource(TEST_IMAGES_FILE_NAME).getInputStream(),
				new ClassPathResource(TEST_LABLES_FILE_NAME).getInputStream());
	}

	protected int[] getLabels(InputStream inputStream) {

		ByteBuffer bb = loadStreamToByteBuffer(inputStream);

		assertMagicNumber(LABEL_FILE_MAGIC_NUMBER, bb.getInt());

		int numLabels = bb.getInt();
		int[] labels = new int[numLabels];

		for (int i = 0; i < numLabels; ++i)
			labels[i] = bb.get() & 0xFF; // To unsigned

		return labels;
	}

	protected List<int[][]> getImages(InputStream inputStream) {
		ByteBuffer bb = loadStreamToByteBuffer(inputStream);

		assertMagicNumber(IMAGE_FILE_MAGIC_NUMBER, bb.getInt());

		int numImages = bb.getInt();
		int numRows = bb.getInt();
		int numColumns = bb.getInt();
		List<int[][]> images = new ArrayList<>();

		for (int i = 0; i < numImages; i++)
			images.add(readImage(numRows, numColumns, bb));

		return images;
	}

	private int[][] readImage(int numRows, int numCols, ByteBuffer bb) {
		int[][] image = new int[numRows][];
		for (int row = 0; row < numRows; row++)
			image[row] = readRow(numCols, bb);
		return image;
	}

	private int[] readRow(int numCols, ByteBuffer bb) {
		int[] row = new int[numCols];
		for (int col = 0; col < numCols; ++col)
			row[col] = bb.get() & 0xFF; // To unsigned
		return row;
	}

	private void assertMagicNumber(int expectedMagicNumber, int magicNumber) {
		if (expectedMagicNumber != magicNumber) {
			switch (expectedMagicNumber) {
			case LABEL_FILE_MAGIC_NUMBER:
				throw new RuntimeException("This is not a label file.");
			case IMAGE_FILE_MAGIC_NUMBER:
				throw new RuntimeException("This is not an image file.");
			default:
				throw new RuntimeException(
						format("Expected magic number %d, found %d", expectedMagicNumber, magicNumber));
			}
		}
	}

	/*******
	 * Just very ugly utilities below here. Best not to subject yourself to them.
	 * ;-)
	 ******/

	private ByteBuffer loadStreamToByteBuffer(InputStream inputStream) {
		return ByteBuffer.wrap(loadFile(inputStream));
	}

	private byte[] loadFile(InputStream inputStream) {
		try {
			byte[] bytes = IOUtils.toByteArray(inputStream);
			return bytes;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}