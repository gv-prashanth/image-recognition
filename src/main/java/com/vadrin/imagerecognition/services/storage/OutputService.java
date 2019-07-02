package com.vadrin.imagerecognition.services.storage;

import org.springframework.stereotype.Service;

@Service
public class OutputService {

	public void renderImage(double[][] image) {
		StringBuffer sb = new StringBuffer();

		for (int row = 0; row < image.length; row++) {
			sb.append("|");
			for (int col = 0; col < image[row].length; col++) {
				int pixelVal = (int) image[row][col];
				if (pixelVal == 0)
					sb.append(" ");
				else if (pixelVal < 256 / 3)
					sb.append(".");
				else if (pixelVal < 2 * (256 / 3))
					sb.append("x");
				else
					sb.append("X");
			}
			sb.append("|\n");
		}

		System.out.println(sb.toString());
	}

	public void renderImage(int[][] image) {
		double[][] castedImage = new double[image.length][image[0].length];
		for(int i=0; i<image.length; i++){
			for(int j=0; j<image[0].length; j++){
				castedImage[i][j] = image[i][j];
			}
		}
		renderImage(castedImage);
	}
	
}
