package com.vadrin.imagerecognition.models;

public class TestDataResult {

	private String[] inputImage;
	private int networkAnswer;
	private int actualAnswer;

	public TestDataResult(String[] inputImage, int networkAnswer, int actualAnswer) {
		super();
		this.inputImage = inputImage;
		this.networkAnswer = networkAnswer;
		this.actualAnswer = actualAnswer;
	}

	public String[] getInputImage() {
		return inputImage;
	}

	public void setInputImage(String[] inputImage) {
		this.inputImage = inputImage;
	}

	public int getNetworkAnswer() {
		return networkAnswer;
	}

	public void setNetworkAnswer(int networkAnswer) {
		this.networkAnswer = networkAnswer;
	}

	public int getActualAnswer() {
		return actualAnswer;
	}

	public void setActualAnswer(int actualAnswer) {
		this.actualAnswer = actualAnswer;
	}

}
