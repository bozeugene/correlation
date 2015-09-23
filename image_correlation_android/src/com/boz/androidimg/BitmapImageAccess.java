package com.boz.androidimg;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.ibm.imgengine.AbstractImage;

public class BitmapImageAccess extends AbstractImage {
	private Bitmap bitmap;
	private Context context;
	
	public BitmapImageAccess(Bitmap bitmap,Context context) {
		super((bitmap == null)?-1:bitmap.getHeight(),(bitmap == null)?-1:bitmap.getWidth());
		this.context = context;
		this.bitmap = bitmap;
	}

	public BitmapImageAccess(String assetFileName,Context context) {
		super(-1,-1);
		AssetManager am = context.getAssets(); 
		InputStream inputStream = null;
		try {
			inputStream = am.open(assetFileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (inputStream != null) {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inPreferredConfig = Bitmap.Config.ARGB_8888;
			bitmap = BitmapFactory.decodeStream(inputStream,null, options);
			height = bitmap.getHeight();
			width = bitmap.getWidth();
		} else {
			bitmap = null;
			height = -1;
			width = -1;
		}
	}

	@Override
	public AbstractImage newImg() {
		return new BitmapImageAccess(Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888),context);
	}

	@Override
	public void writeImg(String name) {
		File outputfile = new File(name);
		outputfile.getParentFile().mkdirs();
		if (outputfile.exists()) {
			outputfile.delete();
		}
		FileOutputStream out = null;
		try {
		    out = new FileOutputStream(name);
		    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
		    // PNG is a lossless format, the compression factor (100) is ignored
		} catch (Exception e) {
		    e.printStackTrace();
		} finally {
		    try {
		        if (out != null) {
		            out.close();
		        }
		    } catch (IOException e) {
		        e.printStackTrace();
		    }
		}
	}

	@Override
	public Object resizeImage(int newHeight, int newWidth) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public short[] getRGBA(int x, int y) {
		short[] ret = new short[PIXEL_INFO_SIZE];
		int argb = bitmap.getPixel(x, y);
		ret[RED] = (short)((argb>>16)&0xFF);
		ret[GREEN] = (short)((argb>>8)&0xFF);
		ret[BLUE] = (short)((argb)&0xFF);
		ret[ALPHA] = (short)((argb>>24)&0xFF);
		return ret;
	}

	@Override
	public void setRGBAG(short[] rgbarray, int x, int y) {
		bitmap.setPixel(x, y, (rgbarray[RED]<<16)|(rgbarray[GREEN]<<8)|(rgbarray[BLUE])|(rgbarray[ALPHA]<<24));
	}

}
