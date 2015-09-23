package com.ibm.imgengine;

public class CorrelationResult {
	public static final char CSV_SEP = ';';
	
	public String referenceName;
	public String [] hypothesisName;
	public double [][] score;
	public int hypothesisSize;
	
	public CorrelationResult(String referenceName,int hypothesisSize) {
		this.referenceName = referenceName;
		this.hypothesisSize = hypothesisSize;
		this.hypothesisName = new String[hypothesisSize];
		this.score = new double[hypothesisSize][AbstractImage.PIXEL_INFO_SIZE];
	}
	
	public static String getCsvHeader() {
		StringBuilder sb = new StringBuilder();
		sb.append("ref name");
		sb.append(CSV_SEP);
		sb.append("hyp name");
		sb.append(CSV_SEP);
		sb.append("av score");
		sb.append(CSV_SEP);
		sb.append("min score");
		sb.append(CSV_SEP);
		sb.append("max score");
		return sb.toString();
	}
	
	private static double max3(double x, double y, double z) {
		if ((x > y) && (x > z)) {
			return x;
		}
		if ((y > x) && (y > z)) {
			return y;
		}
		return z;
	}
	
	private static double min3(double x, double y, double z) {
		if ((x < y) && (x < z)) {
			return x;
		}
		if ((y < x) && (y < z)) {
			return y;
		}
		return z;
	}

	public String toCsv() {
		StringBuilder sb = new StringBuilder();
		for (int i=0; i<hypothesisSize; i++) {
			if (i != 0) {
				sb.append('\n');
			}
			sb.append(referenceName);
			sb.append(CSV_SEP);
			sb.append(hypothesisName[i]);
			double averageScore = (score[i][AbstractImage.RED]+score[i][AbstractImage.GREEN]+score[i][AbstractImage.BLUE])/3.0;
			sb.append(CSV_SEP);
			sb.append(averageScore);
			double min = min3(score[i][AbstractImage.RED],score[i][AbstractImage.GREEN],score[i][AbstractImage.BLUE]);
			sb.append(CSV_SEP);
			sb.append(min);
			double max = max3(score[i][AbstractImage.RED],score[i][AbstractImage.GREEN],score[i][AbstractImage.BLUE]);
			sb.append(CSV_SEP);
			sb.append(max);
		}
		return sb.toString();
	}
}
