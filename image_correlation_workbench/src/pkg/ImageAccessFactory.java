package pkg;

import java.io.File;

import com.ibm.imgengine.AbstractImage;
import com.ibm.imgengine.IImageFactory;

public class ImageAccessFactory implements IImageFactory {

	@Override
	public AbstractImage createFromFile(String fileName) {
		return new BufferedImageAccess(fileName);
	}

	@Override
	public String[] getFileList(String dataName) {
		File dataFolder = new File(dataName);
		String[] ret;
		if (dataFolder.exists()) {
			ret = dataFolder.list();
			for (int i=0; i<ret.length; i++) {
				ret[i] = ret[i];
			}
		} else {
			ret = new String[0];
		}
		return ret;
	}
}
