package pkg;

import java.io.File;

import com.ibm.imgengine.DeepResult;
import com.ibm.imgengine.Engine;
import com.ibm.imgengine.IDisplayCorrelation;
import com.ibm.imgengine.Filter.FilterKind;
import com.ibm.imgengine.IImageFactory;

public class Main implements IDisplayCorrelation {
	
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if ((args == null) || (args.length == 0) || (args.length > 2)) {
			usage();
			return;
		}
		instance().launch(args[0],(args.length == 2)?args[1]:"0");
	}
	
	private static Main __instance;
	
	public static Main instance() {
		if (__instance == null) {
			__instance = new Main();
		}
		return __instance;
	}
	
	public void launch(String folderName,String type) {
		int val = 0;
		try {
			val = Integer.valueOf(type);
		} catch (NumberFormatException e) {
			System.out.println("bad kind value");
			usage();
			return;
		}
		FilterKind kind;
		switch(val) {
			case 1 :
				kind = FilterKind.GREY;
				break;
			case 2 :
				kind = FilterKind.SOBEL;
				break;
			case 0 :
			default :
				kind = FilterKind.NONE;
				break;
		}
		String baseFolder = "ressources"+File.separator+folderName;
		IImageFactory factory = new ImageAccessFactory();
		new Engine(baseFolder,kind,factory,this).run2();
	}
	
	private static void usage() {
		System.out.println("usage : run <data folder name> [filter 0 : none, 1 : grey; 2 : sobel]");
	}

	@Override
	public void displayText(String msg) {
		System.out.println(msg);
	}

	@Override
	public void displayDeepResult(DeepResult result) {
		// TODO Auto-generated method stub
		
	}
	
}
