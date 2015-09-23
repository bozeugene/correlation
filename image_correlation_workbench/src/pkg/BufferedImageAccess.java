package pkg;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.ibm.imgengine.AbstractImage;

public class BufferedImageAccess extends AbstractImage {

	private BufferedImage img;
			
	public BufferedImageAccess(BufferedImage img) {
		super((img == null)?-1:img.getHeight(),(img == null)?-1:img.getWidth());
		this.img = img;
	}

	public BufferedImageAccess(String fileName) {
		super(-1,-1);
		File imgFile = new File(fileName);
		BufferedImage img = null;
		try {
		    img = ImageIO.read(imgFile);
		    //System.out.println(imgFile.getName()+" added to data");
		} catch (IOException e) {
			System.out.println("could not read image : "+imgFile.getAbsolutePath());
			return;
		}		
		this.img = img;
		this.height = img.getHeight();
		this.width = img.getWidth();
	}
	
	@Override
	public Object resizeImage(int height, int width) {
		// TODO code me
		return null;
	}

	@Override
	public short[] getRGBA(int x, int y) {
		short[] ret = new short[PIXEL_INFO_SIZE];
		int argb = img.getRGB(x, y);
		ret[RED] = (short)((argb>>16)&0xFF);
		ret[GREEN] = (short)((argb>>8)&0xFF);
		ret[BLUE] = (short)((argb)&0xFF);
		ret[ALPHA] = (short)((argb>>24)&0xFF);
		return ret;
	}

	@Override
	public void setRGBAG(short[] rgbarray,int x, int y) {
		img.setRGB(x, y, (rgbarray[RED]<<16)|(rgbarray[GREEN]<<8)|(rgbarray[BLUE])|(rgbarray[ALPHA]<<24));
	}

	@Override
	public AbstractImage newImg() {
		return new BufferedImageAccess(new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB));
	}

	@Override
	public void writeImg(String name) {
		File outputfile = new File(name);
		outputfile.getParentFile().mkdirs();
		if (outputfile.exists()) {
			outputfile.delete();
		}
		try {
			outputfile.createNewFile();
		} catch (IOException e) {
			System.out.println("could not create file image : "+name);
			return;
		}		
		try {
			ImageIO.write(img, "png", outputfile);
		} catch (IOException e) {
			System.out.println("could not write image : "+name);
		}
	}
}
