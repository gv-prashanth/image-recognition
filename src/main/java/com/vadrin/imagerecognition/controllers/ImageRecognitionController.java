package com.vadrin.imagerecognition.controllers;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.vadrin.imagerecognition.models.TestDataResult;
import com.vadrin.imagerecognition.services.ImageFormattingService;
import com.vadrin.imagerecognition.services.ImageRecognitionService;
import com.vadrin.neuralnetwork.commons.exceptions.InvalidInputException;
import com.vadrin.neuralnetwork.commons.exceptions.NetworkNotInitializedException;
import com.vadrin.neuralnetwork.services.NeuralNetwork;

@RestController
public class ImageRecognitionController {

	@Autowired
	ImageRecognitionService imageRecognitionService;
	
	@Autowired
	ImageFormattingService imageFormattingService;

	private static final double LEARNINGRATE = 0.02d;
	private static final double INITIALBIASLOWER = -0.5d;
	private static final double INITIALBIASUPPER = 0.7d;
	private static final double INITIALWEIGHTSLOWER = -1d;
	private static final double INITIALWEIGHTSUPPER = 1d;
	private static final int[] NEURONSPEREACHLAYER = { 784, 70, 35, 10 };

	@RequestMapping(method = RequestMethod.GET, value = "/network")
	public NeuralNetwork construct() throws JsonGenerationException, JsonMappingException, InvalidInputException,
			NetworkNotInitializedException, IOException {
		return new NeuralNetwork(NEURONSPEREACHLAYER, LEARNINGRATE, INITIALBIASLOWER, INITIALBIASUPPER,
				INITIALWEIGHTSLOWER, INITIALWEIGHTSUPPER);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/network/score")
	public List<TestDataResult> measure(@RequestBody JsonNode neuralNetworkJson) throws JsonGenerationException,
			JsonMappingException, InvalidInputException, NetworkNotInitializedException, IOException {
		return imageRecognitionService.measureMNISTDataset(new NeuralNetwork(neuralNetworkJson));
	}

	@RequestMapping(method = RequestMethod.PUT, value = "/network/{trainingType}")
	public NeuralNetwork trainStochastic(@RequestBody JsonNode neuralNetworkJson, @PathVariable String trainingType) throws JsonGenerationException,
			JsonMappingException, InvalidInputException, NetworkNotInitializedException, IOException {
		return imageRecognitionService.trainMNISTDataset((new NeuralNetwork(neuralNetworkJson)), trainingType);
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/image/base64png")
	public int aggregate(@RequestBody JsonNode requestObject) throws IOException, InvalidInputException, NetworkNotInitializedException {
		System.out.println(requestObject);
		BufferedImage pngImage = imageFormattingService.getPngImageFromBase64(requestObject.get("base64Png").asText());
		BufferedImage jpegImage = imageFormattingService.convertPngToJpeg(pngImage);
		double finalWidth = 28d;
		double scale = finalWidth / jpegImage.getWidth();
		BufferedImage jpegResizedImage = imageFormattingService.resizeImage(jpegImage, scale);
		BufferedImage greyJpegImage = imageFormattingService.getGreyScaleJpegFromRGBJpeg(jpegResizedImage);
		double[][] pixels = imageFormattingService.getPixelInformation(greyJpegImage);
		return imageRecognitionService.recognize((new NeuralNetwork(requestObject.get("neuralNetworkJson"))), pixels);
	}

}
