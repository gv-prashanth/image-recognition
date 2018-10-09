package com.vadrin.imagerecognition.controllers;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Controller;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vadrin.imagerecognition.services.MnistReader;
import com.vadrin.neuralnetwork.commons.exceptions.InvalidInputException;
import com.vadrin.neuralnetwork.commons.exceptions.NetworkNotInitializedException;
import com.vadrin.neuralnetwork.models.TrainingExample;
import com.vadrin.neuralnetwork.models.DataSet;
import com.vadrin.neuralnetwork.services.NeuralNetwork;

@Controller
public class ImageRecognitionRunner implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(ImageRecognitionRunner.class);
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
	private static final String TRAINING_IMAGES_FILE = "D:\\Projects\\stsworkspace\\image-recognition\\src\\main\\resources\\static\\train-images.idx3-ubyte";
	private static final String TRAINING_LABLES_FILE = "D:\\Projects\\stsworkspace\\image-recognition\\src\\main\\resources\\static\\train-labels.idx1-ubyte";
	private static final String TEST_IMAGES_FILE = "D:\\Projects\\stsworkspace\\image-recognition\\src\\main\\resources\\static\\t10k-images.idx3-ubyte";
	private static final String TEST_LABLES_FILE = "D:\\Projects\\stsworkspace\\image-recognition\\src\\main\\resources\\static\\t10k-labels.idx1-ubyte";

	@Override
	public void run(String... args) throws InvalidInputException, JsonGenerationException, JsonMappingException,
			IOException, NetworkNotInitializedException {
		log.info("Starting the CommandLineRunner at {}", dateFormat.format(new Date()));
		File networkFile = new File(
				"D:\\Projects\\stsworkspace\\image-recognition\\src\\main\\resources\\network.json");
		NeuralNetwork neuralNetwork;
		ObjectMapper mapper = new ObjectMapper();
		if (networkFile.exists() && networkFile.isFile()) {
			JsonNode networkJson = mapper.readValue(networkFile, JsonNode.class);
			neuralNetwork = new NeuralNetwork(networkJson);
			log.info("Found a network file. Loading the previous network file.");
		} else {
			int[] neuronsPerLayer = { 784, 70, 35, 10 };
			neuralNetwork = new NeuralNetwork(neuronsPerLayer, 0.3d, -0.5d, 0.7d, -1d, 1d);
			log.info("No network file found. Constructing a new network.");
		}
//		DataSet trainingSet = createSet(TRAINING_IMAGES_FILE, TRAINING_LABLES_FILE);
//		for (int i =0; i<1000; i++) {
//			DataSet randomTrainingBatch = trainingSet.getRandomSet(0.05);
//			neuralNetwork.train(randomTrainingBatch);
//			double avgrms = neuralNetwork.processAndCompareWithTrainingSetOutput(randomTrainingBatch);
//			neuralNetwork.saveNetworkToFile(networkFile);
//			log.info("{}% Error.", ((int) (avgrms * 100)));
//			//break;
//
//		}
		DataSet testSet = createSet(TEST_IMAGES_FILE, TEST_LABLES_FILE);
		test(neuralNetwork, testSet);
		log.info("Finished the CommandLineRunner at {}", dateFormat.format(new Date()));
	}

	public DataSet createSet(String imagesLoc, String lablesLoc) {
		DataSet set = new DataSet();
		try {
			int[] labels = MnistReader.getLabels(lablesLoc);
			List<int[][]> images = MnistReader.getImages(imagesLoc);
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

	public void test(NeuralNetwork net, DataSet set)
			throws InvalidInputException, NetworkNotInitializedException {
		int correct = 0;

		Iterator<TrainingExample> iterator = set.iterator();
		int i = 0;
		while (iterator.hasNext()) {
			TrainingExample thisExample = iterator.next();
			double highest = indexOfHighestValue(net.process(thisExample.getInput()));
			double actualHighest = indexOfHighestValue(thisExample.getOutput());
			if (highest == actualHighest) {
				correct++;
			}
			if (i % 1000 == 0) {
				System.out.println("Testing finished, RESULT: " + correct + " / " + set.size() + "  -> "
						+ (((double) correct / (double) set.size()) * 100) + " %");
			}
			i++;
		}
	}

	public int indexOfHighestValue(double[] values) {
		int index = 0;
		for (int i = 1; i < values.length; i++) {
			if (values[i] > values[index]) {
				index = i;
			}
		}
		return index;
	}

}
