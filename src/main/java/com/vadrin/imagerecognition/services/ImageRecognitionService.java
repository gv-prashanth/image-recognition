package com.vadrin.imagerecognition.services;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vadrin.neuralnetwork.commons.exceptions.InvalidInputException;
import com.vadrin.neuralnetwork.commons.exceptions.NetworkNotInitializedException;
import com.vadrin.neuralnetwork.models.DataSet;
import com.vadrin.neuralnetwork.models.TrainingExample;
import com.vadrin.neuralnetwork.services.NeuralNetwork;

@Service
public class ImageRecognitionService {

	private static final Logger log = LoggerFactory.getLogger(ImageRecognitionService.class);
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
	private static final String TRAINING_IMAGES_FILE = "D:\\Projects\\stsworkspace\\image-recognition\\src\\main\\resources\\static\\train-images.idx3-ubyte";
	private static final String TRAINING_LABLES_FILE = "D:\\Projects\\stsworkspace\\image-recognition\\src\\main\\resources\\static\\train-labels.idx1-ubyte";
	private static final String TEST_IMAGES_FILE = "D:\\Projects\\stsworkspace\\image-recognition\\src\\main\\resources\\static\\t10k-images.idx3-ubyte";
	private static final String TEST_LABLES_FILE = "D:\\Projects\\stsworkspace\\image-recognition\\src\\main\\resources\\static\\t10k-labels.idx1-ubyte";

	private NeuralNetwork neuralNetwork;
	File networkFile = new File("D:\\Projects\\stsworkspace\\image-recognition\\src\\main\\resources\\network.json");

	@Autowired
	MnistReaderService mnistReaderService;

	@PostConstruct
	private void initiate() throws InvalidInputException, JsonGenerationException, JsonMappingException, IOException,
			NetworkNotInitializedException {
		log.info("Starting the initiation of ImageRecognitionService at {}", dateFormat.format(new Date()));
		if (networkFile.exists() && networkFile.isFile()) {
			ObjectMapper mapper = new ObjectMapper();
			JsonNode networkJson = mapper.readValue(networkFile, JsonNode.class);
			neuralNetwork = new NeuralNetwork(networkJson);
			log.info("Found a network file. Loading the previous network file.");
		} else {
			int[] neuronsPerLayer = { 784, 70, 35, 10 };
			neuralNetwork = new NeuralNetwork(neuronsPerLayer, 0.3d, -0.5d, 0.7d, -1d, 1d);
			log.info("No network file found. Constructing a new network.");
			log.info("Training this newly created neural network");
			train();
			log.info("Finished training the network. Ready for action!");
		}
		log.info("Finished the initiation of ImageRecognitionService at {}", dateFormat.format(new Date()));
	}

	public int recognize(double[][] images) throws InvalidInputException, NetworkNotInitializedException {
		double[] input = new double[28 * 28];
		for (int j = 0; j < 28; j++) {
			for (int k = 0; k < 28; k++) {
				input[k + j * 28] = images[j][k];
			}
		}
		return indexOfHighestValue(neuralNetwork.process(input));
	}

	public double train() throws InvalidInputException, NetworkNotInitializedException, JsonGenerationException,
			JsonMappingException, IOException {
		DataSet trainingSet = createSet(TRAINING_IMAGES_FILE, TRAINING_LABLES_FILE);
		DataSet randomTrainingBatch = trainingSet.getRandomSet(0.1);
		neuralNetwork.train(randomTrainingBatch);
		double avgrms = neuralNetwork.processAndCompareWithTrainingSetOutput(randomTrainingBatch);
		neuralNetwork.saveNetworkToFile(networkFile);
		ObjectMapper mapper = new ObjectMapper();
		JsonNode networkJson = mapper.readValue(networkFile, JsonNode.class);
		neuralNetwork = new NeuralNetwork(networkJson);
		log.info("The network is now trained till an error percentage of {}%.", ((avgrms * 100)));
		return (avgrms * 100);
	}

	public double measure() throws InvalidInputException, NetworkNotInitializedException {
		DataSet testSet = createSet(TEST_IMAGES_FILE, TEST_LABLES_FILE);
		int correct = 0;
		Iterator<TrainingExample> iterator = testSet.iterator();
		int i = 0;
		while (iterator.hasNext()) {
			TrainingExample thisExample = iterator.next();
			double highest = indexOfHighestValue(neuralNetwork.process(thisExample.getInput()));
			double actualHighest = indexOfHighestValue(thisExample.getOutput());
			if (highest == actualHighest) {
				correct++;
			}
			if (i % 1000 == 0) {
				double[][] image = new double[28][28];
				for (int j = 0; j < 28; j++) {
					for (int k = 0; k < 28; k++) {
						 image[j][k] = thisExample.getInput()[k + j * 28];
					}
				}
				printBitmap(image);
			}
			i++;
		}
		return ((double) correct / (double) testSet.size()) * 100;
	}

	private int indexOfHighestValue(double[] values) {
		int index = 0;
		for (int i = 1; i < values.length; i++) {
			if (values[i] > values[index]) {
				index = i;
			}
		}
		return index;
	}

	private DataSet createSet(String imagesLoc, String lablesLoc) {
		DataSet set = new DataSet();
		try {
			int[] labels = mnistReaderService.getLabels(lablesLoc);
			List<int[][]> images = mnistReaderService.getImages(imagesLoc);
			for (int i = 0; i < labels.length; i++) {
				double[] input = new double[28 * 28];
				double[] output = new double[10];
				output[labels[i]] = 1d;
				for (int j = 0; j < 28; j++) {
					for (int k = 0; k < 28; k++) {
						input[k + j * 28] = (double) images.get(i)[j][k] / (double) 256;
					}
				}
				set.add(input, output);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return set;
	}

	public void printBitmap(double[][] pixels) {
		for( int i = 0; i < pixels.length; i++ ) {
		    for( int j = 0; j < pixels[i].length; j++ ) {
		    	System.out.print(pixels[i][j]);
		    }
		    System.out.println();
		}
	}
}
