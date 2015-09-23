package com.ibm.imgengine;


public class Filter {

	public enum FilterKind {
		NONE,GREY,SOBEL
	}
	
	// convert image to grey in native format
	public static void toGrey(AbstractImage img) {
		for (int x=0; x<img.getHeight(); x++) {
			for (int y=0; y<img.getWidth(); y++) {
				short[] rgb = img.getRGBAG(x, y);
				rgb[AbstractImage.RED] = rgb[AbstractImage.GREY];
				rgb[AbstractImage.GREEN] = rgb[AbstractImage.GREY];
				rgb[AbstractImage.BLUE] = rgb[AbstractImage.GREY];
				img.setRGBAG(rgb, x, y);
			}
		}
	}
	
	// convert image to grey in matrix format
	public static void toGrey(ImageMatrix img) {
		short [][][] matrix = img.getMatrix();
		short [] channels; 
		for (int x=0; x<img.getHeight(); x++) {
			for (int y=0; y<img.getWidth(); y++) {
				channels = matrix[x][y];
				// overwrite raw colors
				channels[AbstractImage.RED] = channels[AbstractImage.GREY];
				channels[AbstractImage.GREEN] = channels[AbstractImage.GREY];
				channels[AbstractImage.BLUE] = channels[AbstractImage.GREY];
				// rebuild calculared channels
				matrix[x][y] = AbstractImage.fillChannels(channels);
			}
		}
	}
	
	public static double[][] sobel_x = {{-1,0,1},
										{-2,0,2},
										{-1,0,1}};
	public static double[][] sobel_y = {{1,2,1},
										{0,0,0},
										{-1,-2,-1}};
	
	private static double imgat(AbstractImage img,int x, int y) {
		return (double)(img.getRGBAG(x,y)[AbstractImage.GREY]);
	}

	private static double imgat(short [][][] imageMatrix,int x, int y) {
		return (double)(imageMatrix[x][y][AbstractImage.GREY]);
	}
	
	// extract sobel image in matrix format (filter is done on alpha channel)
	public static void sobelGradient(ImageMatrix img) {
		int sizex = img.getHeight();
		int sizey = img.getWidth();
		double pixel_x;
		double pixel_y;
		double max = 0.0;
		double[][] val = new double[sizex][sizey];
		short [][][] imageMatrix = img.getMatrix();
		for (int x=0; x<sizex; x++) {
			val[x][0] = 0.0;
			val[x][sizey-1] = 0.0;
		}
		for (int y=1; y<sizey-1; y++) {
			val[0][y] = 0.0;
			val[sizex-1][y] = 0.0;
		}
		double locval;
		for (int x=1; x<sizex-1; x++) {
			for (int y=1; y<sizey-1; y++) {
			    pixel_x = (sobel_x[0][0] * imgat(imageMatrix,x-1,y-1)) + (sobel_x[0][1] * imgat(imageMatrix,x,y-1)) + (sobel_x[0][2] * imgat(imageMatrix,x+1,y-1)) +
			              (sobel_x[1][0] * imgat(imageMatrix,x-1,y))   + (sobel_x[1][1] * imgat(imageMatrix,x,y))   + (sobel_x[1][2] * imgat(imageMatrix,x+1,y)) +
			              (sobel_x[2][0] * imgat(imageMatrix,x-1,y+1)) + (sobel_x[2][1] * imgat(imageMatrix,x,y+1)) + (sobel_x[2][2] * imgat(imageMatrix,x+1,y+1));

			    pixel_y = (sobel_y[0][0] * imgat(imageMatrix,x-1,y-1)) + (sobel_y[0][1] * imgat(imageMatrix,x,y-1)) + (sobel_y[0][2] * imgat(imageMatrix,x+1,y-1)) +
			              (sobel_y[1][0] * imgat(imageMatrix,x-1,y))   + (sobel_y[1][1] * imgat(imageMatrix,x,y))   + (sobel_y[1][2] * imgat(imageMatrix,x+1,y)) +
			              (sobel_y[2][0] * imgat(imageMatrix,x-1,y+1)) + (sobel_y[2][1] * imgat(imageMatrix,x,y+1)) + (sobel_y[2][2] * imgat(imageMatrix,x+1,y+1));
			    locval = Math.sqrt((pixel_x*pixel_x)+(pixel_y*pixel_y));
			    val[x][y] = locval;
			    max = (max > locval)?max:locval;
			}
		}
		// set alpha & update filter
		boolean [][] filter = img.getFilter();
		for (int x=0; x<sizex; x++) {
			for (int y=0; y<sizey; y++) {
				locval = 255.0-(val[x][y]*255.0)/max;
				if (locval > 220) {
					imageMatrix[x][y][AbstractImage.ALPHA] = 0;
					filter[x][y] = true;
				} else {
					if (imageMatrix[x][y][AbstractImage.ALPHA] < 90) {
						imageMatrix[x][y][AbstractImage.ALPHA] = 0;
						filter[x][y] = true;
					} else {
						imageMatrix[x][y][AbstractImage.ALPHA] = 255;
						filter[x][y] = false;
					}
				}
			}
		}
	}
	
	// extract sobel image in native format (filter is done on alpha channel)
	public static AbstractImage sobelGradient(AbstractImage img) {
		AbstractImage ret = img.newImg();
		int sizex = img.getHeight();
		int sizey = img.getWidth();
		double pixel_x;
		double pixel_y;
		double max = 0.0;
		double[][] val = new double[sizex][sizey];
		for (int x=0; x<sizex; x++) {
			val[x][0] = 0.0;
			val[x][sizey-1] = 0.0;
		}
		for (int y=1; y<sizey-1; y++) {
			val[0][y] = 0.0;
			val[sizex-1][y] = 0.0;
		}
		double locval;
		for (int x=1; x<sizex-1; x++) {
			for (int y=1; y<sizey-1; y++) {
			    pixel_x = (sobel_x[0][0] * imgat(img,x-1,y-1)) + (sobel_x[0][1] * imgat(img,x,y-1)) + (sobel_x[0][2] * imgat(img,x+1,y-1)) +
			              (sobel_x[1][0] * imgat(img,x-1,y))   + (sobel_x[1][1] * imgat(img,x,y))   + (sobel_x[1][2] * imgat(img,x+1,y)) +
			              (sobel_x[2][0] * imgat(img,x-1,y+1)) + (sobel_x[2][1] * imgat(img,x,y+1)) + (sobel_x[2][2] * imgat(img,x+1,y+1));

			    pixel_y = (sobel_y[0][0] * imgat(img,x-1,y-1)) + (sobel_y[0][1] * imgat(img,x,y-1)) + (sobel_y[0][2] * imgat(img,x+1,y-1)) +
			              (sobel_y[1][0] * imgat(img,x-1,y))   + (sobel_y[1][1] * imgat(img,x,y))   + (sobel_y[1][2] * imgat(img,x+1,y)) +
			              (sobel_y[2][0] * imgat(img,x-1,y+1)) + (sobel_y[2][1] * imgat(img,x,y+1)) + (sobel_y[2][2] * imgat(img,x+1,y+1));
			    locval = Math.sqrt((pixel_x*pixel_x)+(pixel_y*pixel_y));
			    val[x][y] = locval;
			    max = (max > locval)?max:locval;
			}
		}
		short[] rgb;
		for (int x=0; x<sizex; x++) {
			for (int y=0; y<sizey; y++) {
				rgb = img.getRGBAG(x, y);
				locval = 255.0-(val[x][y]*255.0)/max;
				if (locval > 220) {
					rgb[AbstractImage.ALPHA] = 0;
				} else {
					if (rgb[AbstractImage.ALPHA] < 90) {
						rgb[AbstractImage.ALPHA] = 0;
					} else {
						rgb[AbstractImage.ALPHA] = 255;
					}
				}
				ret.setRGBAG(rgb, x, y);
			}
		}
		return ret;
	}
	
}
