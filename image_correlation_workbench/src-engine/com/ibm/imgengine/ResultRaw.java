package com.ibm.imgengine;


public class ResultRaw {
	public double [][][] matrix;
	public String[] header;
	public int size;
	
	public ResultRaw(int size) {
		this.size = size;
		matrix = new double[size][size][AbstractImage.PIXEL_INFO_SIZE];
		header = new String[size];
	}
}
