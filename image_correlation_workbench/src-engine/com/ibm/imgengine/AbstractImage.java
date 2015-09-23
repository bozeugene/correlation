package com.ibm.imgengine;


public abstract class AbstractImage {
	public static final int ALPHA 		= 0;
	public static final int RED 		= 1;
	public static final int GREEN 		= 2;
	public static final int BLUE 		= 3;
	public static final int GREY 		= 4;
	public static final int LUMINANCE 	= 5;
	public static final int CHROM_BLUE 	= 6;
	public static final int CHROM_RED 	= 7;
	
	public static final int PIXEL_INFO_LAST = CHROM_RED;
	public static final int PIXEL_INFO_FIRST = RED;
	public static final int PIXEL_INFO_SIZE = PIXEL_INFO_LAST+1;
	
	protected int height = -1;
	protected int width = -1;

	public AbstractImage(int height,int width) {
		this.height = height;
		this.width = width;
	}
	
	public abstract AbstractImage newImg();
	
	public abstract void writeImg(String name);
	
	public int getHeight() {
		return height;
	}

	public int getWidth() {
		return width;
	}
	
	public static String pixelImage(int kind) {
		switch(kind) {
			case RED :
				return "Red";
			case GREEN :
				return "Green";
			case BLUE :
				return "Blue";
			case ALPHA :
				return "Alpha";
			case GREY :
				return "Grey";
			case LUMINANCE :
				return "Luminance";
			case CHROM_BLUE :
				return "Chrominance blue";
			case CHROM_RED :
				return "Chrominance red";
			default :
				return "???";
		}
	}
	
	protected boolean access(int x, int y) {
		if ((height == -1) || (width == -1)) {
			return false;
		}
		if ((x < 0) || (x >= height) || (y < 0) || (y >= width)) {
			return false;
		}
		return true;
	}
	
	/**
	 * 
	 * @return resized image from original one
	 */
	public abstract Object resizeImage(int newHeight,int newWidth);
	
	public static short[] fillChannels(short[] rgba) {
		short[] ret = new short[PIXEL_INFO_SIZE];
		// raw
		ret[ALPHA] = rgba[ALPHA];
		ret[RED] = rgba[RED];
		ret[GREEN] = rgba[GREEN];
		ret[BLUE] = rgba[BLUE];
		// calculated
		ret[GREY] = (short)((ret[RED] + ret[GREEN] + ret[BLUE])/3);
		double lum = 0.299*ret[RED] + 0.587*ret[GREEN] + 0.114*ret[BLUE];
		ret[LUMINANCE] =  (short)(lum);
		ret[CHROM_BLUE] = (short)(128.0+((((double)ret[BLUE])-lum)/1.772));
		ret[CHROM_RED] = (short)(128.0+((((double)ret[RED])-lum)/1.402));
		return ret;
	}
	
	/**
	 * extend raw channels with grey / luminance / red chrominance / blue chrominance ones
	 * @param x
	 * @param y
	 * @return
	 */
	public short[] getRGBAG(int x, int y) {
		short[] ret = new short[PIXEL_INFO_SIZE];
		if (access(x,y)) {
			ret = fillChannels(getRGBA(x,y));
		} else {
			ret = new short[PIXEL_INFO_SIZE];
		}
		return ret;
	}
	
	/**
	 * return an int array containing pixel info 
	 */
	public abstract short[] getRGBA(int x, int y);
	
	public abstract void setRGBAG(short[] rgbarray,int x, int y);
}
