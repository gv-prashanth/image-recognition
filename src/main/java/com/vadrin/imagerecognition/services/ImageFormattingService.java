package com.vadrin.imagerecognition.services;

import org.springframework.stereotype.Service;

@Service
public class ImageFormattingService {

	//Assumes the argb value is in each element of array
	public String[] constructAsciiStringFromARGBValues(double[][] image) {
		StringBuffer sb = new StringBuffer();

		for (int row = 0; row < image.length; row++) {
			//sb.append("|");
			for (int col = 0; col < image[row].length; col++) {
				int pixelVal = (int) image[row][col];
				if (pixelVal == 0)
					sb.append("&nbsp;&nbsp;&nbsp;");
				else if (pixelVal < 256 / 3)
					sb.append(".");
				else if (pixelVal < 2 * (256 / 3))
					sb.append("x");
				else
					sb.append("X");
			}
			sb.append("\n");
		}

		return sb.toString().split("\n");
	}
	
	public String[] constructAsciiStringFromARGBValues(double[] image, int squareImageWidth) {
		double[][] toConvertToRowsAndCols = new double[squareImageWidth][squareImageWidth];
		for (int j = 0; j < squareImageWidth; j++) {
			for (int k = 0; k < squareImageWidth; k++) {
				toConvertToRowsAndCols[j][k] = image[k + j * squareImageWidth] ;
			}
		}
		return constructAsciiStringFromARGBValues(toConvertToRowsAndCols);
	}
	
}
