package com.boz.androidimg;

import java.io.IOException;

import android.content.Context;
import android.content.res.AssetManager;

import com.ibm.imgengine.AbstractImage;
import com.ibm.imgengine.IImageFactory;

public class ImageBitmapFactory implements IImageFactory {
	private Context context;
	
	public ImageBitmapFactory(Context context) {
		this.context = context;
	}
	
	@Override
	public AbstractImage createFromFile(String fileName) {
		return new BitmapImageAccess(fileName,context);
	}

	@Override
	public String[] getFileList(String dataFolderName) {
		AssetManager am = context.getAssets();
		String[] ret;
		try {
			ret = am.list(dataFolderName);
		} catch (IOException e) {
			e.printStackTrace();
			ret = new String[0];
		}
		return ret;
	}

}
