package com.ibm.imgengine;


public interface IImageFactory {

	public AbstractImage createFromFile(String fileName);
	
	public String[] getFileList(String dataFolderName);
	
}
