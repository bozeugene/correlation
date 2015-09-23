package com.ibm.imgengine;

import java.util.ArrayList;

public class DeepResult {
	public ArrayList<Entry>[][] sortedResult;
	public int numberOfImages;
	public int numberOfChannel;
	public String dataFolderName;
	public String[] imageNames;
	
	@SuppressWarnings("unchecked")
	public DeepResult(int numberOfImages,int numberOfChannel,String dataFolderName,String[] imageNames) {
		this.numberOfImages = numberOfImages;
		this.numberOfChannel = numberOfChannel;
		this.sortedResult = new ArrayList[numberOfImages][numberOfChannel];
		this.dataFolderName = dataFolderName;
		this.imageNames = imageNames;
	}
	
	public class Entry {
		
		public double score;
		public int imageIndex;
		
		public Entry(int imageIndex,double score) {
			this.imageIndex = imageIndex;
			this.score = score;
		}
	}
}
