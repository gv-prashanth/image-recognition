package com.vadrin.imagerecognition.services.storage;

import static java.lang.String.format;

import java.io.ByteArrayOutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class MnistReaderService {
	private static final int LABEL_FILE_MAGIC_NUMBER = 2049;
	private static final int IMAGE_FILE_MAGIC_NUMBER = 2051;

	protected int[] getLabels(String infile) {

		ByteBuffer bb = loadFileToByteBuffer(infile);

		assertMagicNumber(LABEL_FILE_MAGIC_NUMBER, bb.getInt());

		int numLabels = bb.getInt();
		int[] labels = new int[numLabels];

		for (int i = 0; i < numLabels; ++i)
			labels[i] = bb.get() & 0xFF; // To unsigned

		return labels;
	}

	protected List<int[][]> getImages(String infile) {
		ByteBuffer bb = loadFileToByteBuffer(infile);

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
	 * Just very ugly utilities below here. Best not to subject yourself to
	 * them. ;-)
	 ******/

	private ByteBuffer loadFileToByteBuffer(String infile) {
		return ByteBuffer.wrap(loadFile(infile));
	}

	private byte[] loadFile(String infile) {
		try {
			RandomAccessFile f = new RandomAccessFile(infile, "r");
			FileChannel chan = f.getChannel();
			long fileSize = chan.size();
			ByteBuffer bb = ByteBuffer.allocate((int) fileSize);
			chan.read(bb);
			bb.flip();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			for (int i = 0; i < fileSize; i++)
				baos.write(bb.get());
			chan.close();
			f.close();
			return baos.toByteArray();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}