package com.vadrin.imagerecognition.services.storage;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.vadrin.neuralnetwork.models.DataSet;

@Service
public class DataStorageService {
	
	private static final String TRAINING_IMAGES_FILE_NAME = "train-images.idx3-ubyte";
	private static final String TRAINING_LABLES_FILE_NAME = "train-labels.idx1-ubyte";
	private static final String TEST_IMAGES_FILE_NAME = "t10k-images.idx3-ubyte";
	private static final String TEST_LABLES_FILE_NAME = "t10k-labels.idx1-ubyte";

	@Autowired
	private MnistReaderService mnistReaderService;
	
	@Autowired
	private OutputService outputService;
	
	//TODO: Hardcoded 28 is bad. Need to fix later.
	private DataSet createSet(String imagesLoc, String lablesLoc) {
		DataSet set = new DataSet();
		try {
			int[] labels = mnistReaderService.getLabels(lablesLoc);
			List<int[][]> images = mnistReaderService.getImages(imagesLoc);
			for (int i = 0; i < labels.length; i++) {
				double[] input = new double[28 * 28];
				double[] output = new double[10];
				output[labels[i]] = 1d;
//				if(i%10==0)
//					outputService.renderImage(images.get(i));
				for (int j = 0; j < 28; j++) {
					for (int k = 0; k < 28; k++) {
						//input[k + j * 28] = (double) images.get(i)[j][k] / (double) 256;
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
		return createSet(new ClassPathResource(TRAINING_IMAGES_FILE_NAME).getFile().getPath(),
				new ClassPathResource(TRAINING_LABLES_FILE_NAME).getFile().getPath());
	}
	
	public DataSet getTestSet() throws IOException {
		return createSet(new ClassPathResource(TEST_IMAGES_FILE_NAME).getFile().getPath(),
				new ClassPathResource(TEST_LABLES_FILE_NAME).getFile().getPath());
	}
	
}
