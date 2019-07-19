package com.vadrin.imagerecognition.services;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.vadrin.imagerecognition.services.storage.DataStorageService;
import com.vadrin.imagerecognition.services.storage.NetworkStorageService;
import com.vadrin.neuralnetwork.commons.exceptions.InvalidInputException;
import com.vadrin.neuralnetwork.commons.exceptions.NetworkNotInitializedException;
import com.vadrin.neuralnetwork.models.DataSet;
import com.vadrin.neuralnetwork.models.TrainingExample;
import com.vadrin.neuralnetwork.services.NeuralNetwork;

@Service
public class ImageRecognitionService {

	private static final Logger log = LoggerFactory.getLogger(ImageRecognitionService.class);
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
	private static final int LOOPS = 100;
	private static final int EPOCHS = 50;
	private static final double SIZEFACTOR = 0.01d;
	private static final double LEARNINGRATE = 0.02d;

	private NeuralNetwork neuralNetwork;

	@Autowired
	DataStorageService dataStorageService;
	
	@Autowired
	NetworkStorageService networkStorageService;

	@PostConstruct
	private void initiate() throws InvalidInputException, JsonGenerationException, JsonMappingException, IOException,
			NetworkNotInitializedException {
		log.info("Starting the initiation of ImageRecognitionService at {}", dateFormat.format(new Date()));
		if(!networkStorageService.isNetworkPresentInStorage()) {
			log.info("Network file NOT found. Loading a new random network.");
			networkStorageService.createEmptyNetworkStorage();
			int[] neuronsPerLayer = { 784, 70, 35, 10 };
			neuralNetwork = new NeuralNetwork(neuronsPerLayer, LEARNINGRATE, -0.5d, 0.7d, -1d, 1d);
			networkStorageService.saveNetworkToStorage(neuralNetwork);
		}
		log.info("Found a network file. Loading it.");
		neuralNetwork = networkStorageService.loadNetworkFromStorage();
		log.info("Finished the initiation of ImageRecognitionService at {}", dateFormat.format(new Date()));
	}

	public int recognize(double[][] images) throws InvalidInputException, NetworkNotInitializedException {
		//Assuming square inputs
		double[] input = new double[images.length * images.length];
		for (int j = 0; j < images.length; j++) {
			for (int k = 0; k < images.length; k++) {
				input[k + j * images.length] = images[j][k];
			}
		}
		return indexOfHighestValue(neuralNetwork.process(input));
	}

	public void train() throws InvalidInputException, NetworkNotInitializedException, JsonGenerationException,
			JsonMappingException, IOException {
		log.info("Training this neural network from MNIST dataset");
		DataSet trainingSet = dataStorageService.getTrainingSet();
		for(int epoch=0; epoch < EPOCHS; epoch++) {
			DataSet randomTrainingBatch = trainingSet.getRandomSet(SIZEFACTOR);
			log.info("Picked up a random training batch {} of size {}", epoch, randomTrainingBatch.size());
			for(int i=0; i < LOOPS; i++) {
				neuralNetwork.trainALittle(randomTrainingBatch);
//				double accuracy = measure(dataStorageService.getTrainingSet());
//				log.info("Completed training this random batch {} after {} iterations of backprop. And the current accuracy is {}", epoch, LOOPS, accuracy);
//				networkStorageService.saveNetworkToStorage(neuralNetwork);
			}
			double accuracy = measure(dataStorageService.getTrainingSet());
			log.info("Completed training this random batch {} after {} iterations of backprop. And the current accuracy is {}", epoch, LOOPS, accuracy);
			networkStorageService.saveNetworkToStorage(neuralNetwork);
		}
	}

	public double measure() throws InvalidInputException, NetworkNotInitializedException, IOException {
		DataSet testSet = dataStorageService.getTestSet();
		return measure(testSet);
	}
	
	public double measure(DataSet inputSet) throws InvalidInputException, NetworkNotInitializedException, IOException {
		int correct = 0;
		Iterator<TrainingExample> iterator = inputSet.iterator();
		while (iterator.hasNext()) {
			TrainingExample thisExample = iterator.next();
			int networkAnswer = indexOfHighestValue(neuralNetwork.process(thisExample.getInput()));
			int actualAnswer = indexOfHighestValue(thisExample.getOutput());
			if (networkAnswer == actualAnswer) {
				correct++;
			}
		}
		return ((double) correct / (double) inputSet.size()) * 100;
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


}
