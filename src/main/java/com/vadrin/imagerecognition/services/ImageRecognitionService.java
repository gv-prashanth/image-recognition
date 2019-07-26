package com.vadrin.imagerecognition.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.vadrin.imagerecognition.models.TestDataResult;
import com.vadrin.neuralnetwork.commons.exceptions.InvalidInputException;
import com.vadrin.neuralnetwork.commons.exceptions.NetworkNotInitializedException;
import com.vadrin.neuralnetwork.models.DataSet;
import com.vadrin.neuralnetwork.models.TrainingExample;
import com.vadrin.neuralnetwork.services.NeuralNetwork;

@Service
public class ImageRecognitionService {

	private static final Logger log = LoggerFactory.getLogger(ImageRecognitionService.class);

	private static final int EPOCHS = 2;
	private static final double SIZEFACTOR = 0.01d;

	@Autowired
	private MnistReaderService mnistReaderService;
	
	@Autowired
	ImageFormattingService imageFormattingService;
	
	@Value("${com.vadrin.imagerecognition.isheroku: false}")
	private boolean isHeroku;

	public NeuralNetwork trainMNISTDataset(NeuralNetwork neuralNetwork) throws InvalidInputException,
			NetworkNotInitializedException, JsonGenerationException, JsonMappingException, IOException {
		log.info("Training this neural network from MNIST dataset");
		DataSet fullTrainingSet;
		//TODO: This is only solve the memory issue incase you are deploying on heroku.
		if(isHeroku) {
			fullTrainingSet = mnistReaderService.getSmallTrainingSet();
		}else {
			fullTrainingSet = mnistReaderService.getTrainingSet();
		}
		for (int epoch = 0; epoch < EPOCHS; epoch++) {
			neuralNetwork.trainUsingMiniBatchGradientDescent(fullTrainingSet, SIZEFACTOR);
			log.info("Completed training this full batch. Current epoch number is {}.", epoch);
		}
		return neuralNetwork;
	}

	public List<TestDataResult> measureMNISTDataset(NeuralNetwork neuralNetwork)
			throws InvalidInputException, NetworkNotInitializedException, IOException {
		log.info("Measuring this neural network from MNIST Testset");
		List<TestDataResult> toReturn = new ArrayList<TestDataResult>();
		DataSet testSet = mnistReaderService.getTestSet();
		int counter = 0;
		Iterator<TrainingExample> iterator = testSet.iterator();
		while (iterator.hasNext()) {
			TrainingExample thisExample = iterator.next();
			int networkAnswer = indexOfHighestValue(neuralNetwork.process(thisExample.getInput()));
			int actualAnswer = indexOfHighestValue(thisExample.getOutput());
			if(counter%50==0) {
				toReturn.add(new TestDataResult(imageFormattingService.constructAsciiStringFromARGBValues(thisExample.getInput(), 28), networkAnswer, actualAnswer));
			}
//			if (networkAnswer == actualAnswer) {
//				correct++;
//			}
			counter++;
		}
//		return ((double) correct / (double) testSet.size()) * 100;
		return toReturn;
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
