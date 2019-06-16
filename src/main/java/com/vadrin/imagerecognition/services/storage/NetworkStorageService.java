package com.vadrin.imagerecognition.services.storage;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vadrin.neuralnetwork.services.NeuralNetwork;

@Service
public class NetworkStorageService {

	private static final Logger log = LoggerFactory.getLogger(NetworkStorageService.class);
	private static final String NETWORK_FILE_NAME = "network.json";
	private File networkFile;
	
	public void saveNetworkToStorage(NeuralNetwork neuralNetwork) throws JsonGenerationException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.writeValue(networkFile, neuralNetwork);
		log.info("Saved the network to file {}", networkFile.getAbsolutePath());
	}
	
	public NeuralNetwork loadNetworkFromStorage() throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode networkJson = mapper.readValue(networkFile, JsonNode.class);
		return new NeuralNetwork(mapper.convertValue(networkJson.get("neuronsPerLayer"), int[].class),
				mapper.convertValue(networkJson.get("learningRate"), double.class),
				mapper.convertValue(networkJson.get("neuronBiases"), double[][].class),
				mapper.convertValue(networkJson.get("networkWeights"), double[][][].class));
	}
	
	public void createEmptyNetworkStorage() throws IOException {
		File file = new File(getClass().getClassLoader().getResource(".").getFile() + "/"+NETWORK_FILE_NAME);
		file.createNewFile();
	}

	public boolean isNetworkPresentInStorage() throws IOException {
		networkFile = new ClassPathResource(NETWORK_FILE_NAME).getFile();
		return ((networkFile.exists()) && (networkFile.isFile()));
	}
	
	
}
