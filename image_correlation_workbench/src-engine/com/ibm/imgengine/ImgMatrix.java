package com.ibm.imgengine;

public class ImgMatrix {
	
	public enum CorrelationKind {
		NCC,
		ZNCC,
		ZSSD
	}
	
	private int height;
	private int width;
	private short [][][] matrix;
	private double[] mean;
	private double[] sumdeltasquare;
	private double[] sumsquare;
	private String name;

	public ImgMatrix(AbstractImage img,String name) {
		this.height = img.getHeight();
		this.width = img.getWidth();
		this.name = name;
		fillMatrix(img);
		computeMeanAndStdDev();
	}
	
	public int getHeight() {
		return height;
	}
	
	public int getWidth() {
		return width;
	}
	
	public short[][][] getMatrix() {
		return matrix;		
	}
		
	public String getName() {
		return name;
	}
	
	private void fillMatrix(AbstractImage img) {
		matrix = new short [height][width][AbstractImage.PIXEL_INFO_SIZE];
		// extract pixel values
		for (int x=0; x<height; x++) {
			for (int y=0; y<width; y++) {
				short[] rgbag = img.getRGBAG(x, y);
				for (int k=0; k<AbstractImage.PIXEL_INFO_SIZE; k++) {
					matrix[x][y][k] = rgbag[k];
				}
			}
		}
	}
	
	private void computeMeanAndStdDev() {
		mean = new double[AbstractImage.PIXEL_INFO_SIZE];
		sumdeltasquare = new double[AbstractImage.PIXEL_INFO_SIZE];
		sumsquare = new double[AbstractImage.PIXEL_INFO_SIZE];
		for (int k=0; k<AbstractImage.PIXEL_INFO_SIZE; k++) {
			mean[k] = 0;
			sumdeltasquare[k] = 0;
			sumsquare[k] = 0;
		}
		short[][] line;
		short[] pixel;
		short channel;
		for (int x=0; x<height; x++) {
			line = matrix[x];
			for (int y=0; y<width; y++) {
				pixel = line[y];
				for (int k=0; k<AbstractImage.PIXEL_INFO_SIZE; k++) {
					mean[k] += pixel[k];
				}
			}
		}
		int nbpixel = height*width;
		for (int k=0; k<AbstractImage.PIXEL_INFO_SIZE; k++) {
			mean[k] /= (double)nbpixel;			
//			sumsquare[k] = Math.sqrt(sumdeltasquare[k]);
//			sumdeltasquare[k] = Math.sqrt(sumdeltasquare[k]-(mean[k]*mean[k])); 
		}
		double val;
		for (int x=0; x<height; x++) {
			line = matrix[x];
			for (int y=0; y<width; y++) {
				pixel = line[y];
				for (int k=0; k<AbstractImage.PIXEL_INFO_SIZE; k++) {
					channel = pixel[k];
					val = ((double)channel)-mean[k];
					sumdeltasquare[k] += val*val;
					sumsquare[k] += channel*channel;
				}
			}
		}
		for (int k=0; k<AbstractImage.PIXEL_INFO_SIZE; k++) {
			sumdeltasquare[k] = Math.sqrt(sumdeltasquare[k]);
			sumsquare[k] = Math.sqrt(sumsquare[k]);
		}
	}
	
	public double[] correlate(ImgMatrix ref,CorrelationKind kind) {
		double[] ret = new double[AbstractImage.PIXEL_INFO_SIZE];
		for (int k=0; k<AbstractImage.PIXEL_INFO_SIZE; k++) {
			ret[0] = 0;
		}
		double [] refmean = ref.mean;
		double [] refsumsquare = ref.sumsquare;
		double [] refsumdeltasquare = ref.sumdeltasquare;
		short [][][] refmatrix = ref.matrix;
		short [][] line;
		short [][] refline;
		short [] pixel;
		short [] refpixel;
		double val;
		for (int x=0; x<height; x++) {
			line = matrix[x];
			refline = refmatrix[x];
			for (int y=0; y<width; y++) {
				pixel = line[y];
				refpixel = refline[y];
				for (int k=0; k<AbstractImage.PIXEL_INFO_SIZE; k++) {
					switch(kind) {
						case NCC :
							ret[k] += ((double)pixel[k])*((double)refpixel[k]);
							break;
						case ZNCC :
							ret[k] += ((double)pixel[k]-mean[k])*((double)refpixel[k]-refmean[k]);
							break;
						case ZSSD :
							val = (((double)pixel[k]-mean[k])/sumdeltasquare[k])-(((double)refpixel[k]-refmean[k])/refsumdeltasquare[k]);
							ret[k] += val*val;
							break;
					}
				}				
			}			
		}
		for (int k=0; k<AbstractImage.PIXEL_INFO_SIZE; k++) {
			switch(kind) {
				case NCC :
					ret[k] /= refsumsquare[k]*sumsquare[k];
					break;
				case ZNCC :
					ret[k] /= refsumdeltasquare[k]*sumdeltasquare[k];
					break;
				case ZSSD :
					// nothing
					break;
			}
		}
		return ret;
	}
}
