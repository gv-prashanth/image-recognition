package com.vadrin.imagerecognition.controllers;

import java.awt.image.BufferedImage;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.vadrin.imagerecognition.services.ImageRecognitionService;
import com.vadrin.imagerecognition.services.format.ImageFormattingService;
import com.vadrin.imagerecognition.services.storage.OutputService;
import com.vadrin.neuralnetwork.commons.exceptions.InvalidInputException;
import com.vadrin.neuralnetwork.commons.exceptions.NetworkNotInitializedException;

@RestController
public class ImageRecognitionController {
	
	@Autowired
	ImageRecognitionService imageRecognitionService;
	
	@Autowired
	ImageFormattingService imageFormattingService;
	
	@Autowired
	OutputService outputService;
	
	@RequestMapping(method = RequestMethod.POST, value = "/image/base64png")
	public int aggregate(@RequestBody String base64Png) throws IOException, InvalidInputException, NetworkNotInitializedException {
		BufferedImage pngImage = imageFormattingService.getPngImageFromBase64(base64Png);
		BufferedImage jpegImage = imageFormattingService.convertPngToJpeg(pngImage);
		double finalWidth = 28d;
		double scale = finalWidth / jpegImage.getWidth();
		BufferedImage jpegResizedImage = imageFormattingService.resizeImage(jpegImage, scale);
		BufferedImage greyJpegImage = imageFormattingService.getGreyScaleJpegFromRGBJpeg(jpegResizedImage);
		double[][] pixels = imageFormattingService.getPixelInformation(greyJpegImage);
		//outputService.renderImage(pixels);
		return imageRecognitionService.recognize(pixels);
	}
	
	@RequestMapping(method = RequestMethod.PUT, value = "/network")
	public void train() throws JsonGenerationException, JsonMappingException, InvalidInputException, NetworkNotInitializedException, IOException {
		imageRecognitionService.train();
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/network/measure")
	public double measure() throws JsonGenerationException, JsonMappingException, InvalidInputException, NetworkNotInitializedException, IOException {
		return imageRecognitionService.measure();
	}

}
