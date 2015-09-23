package com.ibm.imgengine;

public class ResultSimplified {
	
	public double score;
	public int imageIndex;
	
	public ResultSimplified(int imageIndex,double score) {
		this.imageIndex = imageIndex;
		this.score = score;
	}
}
