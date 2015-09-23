package com.ibm.imgengine;

public class ImageMatrix {
	private int height;
	private int width;
	private short [][][] matrix;
	private boolean valid = false;
	private double[] mean;
	private double[] squareDist;
	private boolean[] constant;
	private String name;
	private boolean [][] filter;
	
	public ImageMatrix(AbstractImage img,String name) {
		this.valid = init(img);
		this.name = name;
		if (valid) {
			computeFilter();
			computeMean();
			computeSquareDist();
		}
	}
	
	public String getName(){
		return name;
	}
	
	public boolean isValid() {
		return valid;
	}

	public int getHeight() {
		return height;
	}
	
	public int getWidth() {
		return width;
	}
	
	public double[] getMean() {
		return mean;
	}
	
	public double[] getSquareDist() {
		return squareDist;
	}
	
	public short[][][] getMatrix() {
		return matrix;		
	}
	
	public boolean[] getConstant() {
		return constant;
	}
	
	public boolean [][] getFilter() {
		return filter;
	}
	
	private boolean init(AbstractImage img) {
		if (img == null) {
			return false;
		}
		this.height = img.getHeight();
		this.width = img.getWidth();
		if ((height <= 0) || (width <= 0)) {
			return false;
		}
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
		return true;
	}

	private void computeFilter() {
		this.filter = new boolean[height][width];
		for (int x=0; x<height; x++) {
			for (int y=0; y<width; y++) {
				filter[x][y] = false;//(matrix[x][y][AbstractImage.ALPHA] < 90);
			}
		}
	}
	
	private void computeMean() {
		short[] min = new short[AbstractImage.PIXEL_INFO_SIZE];
		short[] max = new short[AbstractImage.PIXEL_INFO_SIZE];
		long [] sum = new long[AbstractImage.PIXEL_INFO_SIZE];
		for (int i=AbstractImage.PIXEL_INFO_FIRST; i<AbstractImage.PIXEL_INFO_SIZE; i++) {
			min[i] = 255;
			max[i] = 0;
			sum[i] = 0;
		}
		short val;
		long nbval = 0;
		for (int x=0; x<height; x++) {
			for (int y=0; y<width; y++) {
				if (!filter[x][y]) {
					for (int k=AbstractImage.PIXEL_INFO_FIRST; k<AbstractImage.PIXEL_INFO_SIZE; k++) {
						val = matrix[x][y][k];
						sum[k] += val;
						min[k] = (val < min[k])?val:min[k];
						max[k] = (val > max[k])?val:max[k];
						nbval++;
					}
				}
			}
		}
		constant = new boolean[AbstractImage.PIXEL_INFO_SIZE];
		for (int i=AbstractImage.PIXEL_INFO_FIRST; i<AbstractImage.PIXEL_INFO_SIZE; i++) {
			constant[i] = (min[i] == max[i]);
		}
		this.mean = new double[AbstractImage.PIXEL_INFO_SIZE];
		for (int i=AbstractImage.PIXEL_INFO_FIRST; i<AbstractImage.PIXEL_INFO_SIZE; i++) {
			this.mean[i] = ((double)sum[i])/((double)nbval);
		}
	}
	
	private void computeSquareDist() {
		this.squareDist = new double[AbstractImage.PIXEL_INFO_SIZE];
		if (!valid) {
			return;
		}
		short[][] line;
		short[] pixel;
		double val;
		for (int i=0; i<height; i++) {
			line = matrix[i];
			for (int j=0; j<width; j++) {
				if (!filter[i][j]) {
					pixel = line[j];
					for (int k=AbstractImage.PIXEL_INFO_FIRST; k<AbstractImage.PIXEL_INFO_SIZE; k++) {
						val = (pixel[k]-mean[k]);
						this.squareDist[k] += val*val;
					}
				}
			}
		}
	}
	
	public double[] correlate(ImageMatrix reference) {
		double[] ret = new double[AbstractImage.PIXEL_INFO_SIZE];
		if ((!valid) || (reference == null) || (!reference.isValid())) {
			System.out.println("image and reference must be valid");
			return ret;
		}
		if ((height != reference.getHeight()) || (width != reference.width)) {
			System.out.println("image and reference must share shame dimension");
			return ret;
		}
		long part1;
		long part2;
		double[] denominator = new double[AbstractImage.PIXEL_INFO_SIZE];
		double[] numerator = new double[AbstractImage.PIXEL_INFO_SIZE];
		boolean[] refconstant = reference.getConstant();
		for (int i=AbstractImage.PIXEL_INFO_FIRST; i<AbstractImage.PIXEL_INFO_SIZE; i++) {
			part1 = (long)((constant[i]) ? 1 : (squareDist[i]));
			part2 = (long)((refconstant[i]) ? 1 : (reference.getSquareDist()[i]));
			denominator[i] = Math.sqrt(part1*part2);
			numerator[i] = 0;
		}
		double[] refmean = reference.getMean();
		short[][][] refmatrix = reference.getMatrix();
		short[][] refline,line;
		short[] refpixel,pixel;
		boolean [][] refFilter = reference.getFilter();
		double part11, part22;
		for (int i=0; i<height; i++) {
			refline = refmatrix[i];
			line 	= matrix[i];
			for (int j=0; j<width; j++) {
				if ((!filter[i][j]) && (!refFilter[i][j])) {
					refpixel = refline[j];
					pixel 	 = line[j];
					for (int k=AbstractImage.PIXEL_INFO_FIRST; k<AbstractImage.PIXEL_INFO_SIZE; k++) {
						part11 = ((constant[k]) ? 1 : (pixel[k]-mean[k]));
						part22 = ((refconstant[k]) ? 1 : (refpixel[k]-refmean[k]));
						numerator[k] += part11*part22;
					}
				}
			}
		}
		for (int i=AbstractImage.PIXEL_INFO_FIRST; i<AbstractImage.PIXEL_INFO_SIZE; i++) {
			if ((constant[i]) && (reference.getConstant()[i])) {
				ret[i] = (255.0-Math.abs(mean[i]-reference.getMean()[i]))/255.0;				
			} else {
				ret[i] = numerator[i]/denominator[i];
			}
		}
		return ret;
	}
	
	public void display(double[] correlation,String name) {
		StringBuilder sb = new StringBuilder();
		sb.append(name);
		for (int i=AbstractImage.PIXEL_INFO_FIRST; i<AbstractImage.PIXEL_INFO_SIZE; i++) {
			sb.append("  \t");
			sb.append(AbstractImage.pixelImage(i));
			sb.append(" ");
			sb.append(correlation[i]);
		}
		System.out.println(sb.toString());
	}


}
