package com.ibm.imgengine;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.ibm.imgengine.Filter.FilterKind;
import com.ibm.imgengine.ImgMatrix.CorrelationKind;

public class Engine {
	private static final boolean STORE_FILTERED_IMAGE = false;
	private static final boolean GENERATE_CSV = false;
	private static final boolean BASIC_ANALYZE = false;
	private static final boolean DEEP_ANALYZE = true;
	private static final boolean DEEP_TEXT_DISPLAY = true;
	
	private String dataFolderName; 
	private FilterKind filterKind;
	private IImageFactory factory;
	private IDisplayCorrelation display;
	
	public Engine(String dataFolderName, FilterKind filterKind,IImageFactory factory,IDisplayCorrelation display) {
		this.dataFolderName = dataFolderName;
		this.filterKind = filterKind;
		this.factory = factory;
		this.display = display;
	}
	
	public void run() {
		List<ImageMatrix> data = null;//readImageFolder(dataFolderName);
		ResultRaw result = correlate(data);
		
		if (GENERATE_CSV) {
			String rawOutputFileName = dataFolderName+File.separator+"raw.csv";
			rawDisplay(result,rawOutputFileName);
		}
		display.displayText("analyzing result...");
		if (BASIC_ANALYZE) {
			double[][] delta = deltaCorrelation(result);
			if (GENERATE_CSV) {
				String deltaOutputFileName = dataFolderName+File.separator+"delta.csv";			
				deltaDisplay(result,delta,deltaOutputFileName);
			}
		}
		if (DEEP_ANALYZE) {
			DeepResult sortedResult = analyze(result);
			if (DEEP_TEXT_DISPLAY) {
				displayDeepResultAsText(sortedResult);
			}
			display.displayDeepResult(sortedResult);
		}
	}

	public void run2() {
		filterKind = FilterKind.NONE;
		List<ImgMatrix> allreference = readImageFolder("ressources"+File.separator+"cat"+File.separator+"in_step");
		List<ImgMatrix> allhypothesys = readImageFolder("ressources"+File.separator+"cat"+File.separator+"in_screen");
		List<CorrelationResult> allResult = new ArrayList<CorrelationResult>();
		for (ImgMatrix reference:allreference) {
			allResult.add(correlate(reference,allhypothesys,CorrelationKind.NCC));
		}
		correlationresultCsvDisplay(allResult,"ressources"+File.separator+"cat"+File.separator+"cr_ncc.csv");
		allResult.clear();
		for (ImgMatrix reference:allreference) {
			allResult.add(correlate(reference,allhypothesys,CorrelationKind.ZNCC));
		}
		correlationresultCsvDisplay(allResult,"ressources"+File.separator+"cat"+File.separator+"cr_zncc.csv");
		allResult.clear();
		for (ImgMatrix reference:allreference) {
			allResult.add(correlate(reference,allhypothesys,CorrelationKind.ZSSD));
		}
		correlationresultCsvDisplay(allResult,"ressources"+File.separator+"cat"+File.separator+"cr_ZSSD.csv");
	}
	
	private List<ImgMatrix> readImageFolder(String imageFolderName) {
		long startTime,endTime;
		String[] allFileNames = factory.getFileList(imageFolderName);
		if (allFileNames.length == 0) {
			System.out.println(imageFolderName+" must exist and contains images");
			return null;
		}
		// =============================== reading files ================================
		List<AbstractImage> allImages = new ArrayList<AbstractImage>();
		List<String> imageNames = new ArrayList<String>();
		for(String fileName:allFileNames) {
			if (validEnding(fileName)) {
				String fullFileName = dataFolderName+File.separator+fileName;
				allImages.add(factory.createFromFile(fullFileName));
				imageNames.add(fileName);
			}
		}
		// =============================== building structure + filtering ================================
		display.displayText("filtering...");
		startTime = System.currentTimeMillis();
		List<ImgMatrix> ret = new ArrayList<ImgMatrix>();
		int cpt = 0;
		for (AbstractImage image:allImages) {
			ImgMatrix imageMatrix = new ImgMatrix(image,imageNames.get(cpt));
			switch(filterKind) {
				case NONE :
					break;
				case GREY :
//					Filter.toGrey(imageMatrix);
					break;
				case SOBEL :
//					Filter.sobelGradient(imageMatrix);						
					break;
			}
//			if (STORE_FILTERED_IMAGE && (filterKind != FilterKind.NONE)) {
//				image.writeImg(dataFolderName+File.separator+"filtered"+File.separator+imageNames.get(cpt));
//			}
			ret.add(imageMatrix);
			cpt ++;
		}
		endTime = System.currentTimeMillis();
		display.displayText(cpt+" struct created and filtered in "+(endTime-startTime)+" ms");
		return ret;		
	}
	
	private boolean validEnding(String fileName) {
		if (fileName == null) {
			return false;
		}
		int pos = fileName.lastIndexOf('.');
		if (pos == -1) {
			return false;
		}
		if (pos+1 >= fileName.length()) {
			return false;
		}
		String ending = fileName.substring(pos+1, fileName.length());
		if (ending == null) {
			return false;
		}
		if (ending.equals("png")) {
			return true;
		}
//		if (ending.equals("jpg")) {
//			return true;
//		}
		return false;
	}
	
	private CorrelationResult correlate(ImgMatrix reference,List<ImgMatrix> allHypothesys,CorrelationKind kind) {		
		if ((allHypothesys == null) || (allHypothesys.size() == 0) || (reference == null)) {
			System.out.println("data folder must contains png or jpg images");
			return null;
		}
		long startTime = System.currentTimeMillis();
		int size = allHypothesys.size();
		CorrelationResult result = new CorrelationResult(reference.getName(),size);	
		int i = 0;
		for (ImgMatrix hypothesys:allHypothesys) {
			result.score[i] = reference.correlate(hypothesys,kind);
			result.hypothesisName[i] = hypothesys.getName();
			i++;
		}
		long endTime = System.currentTimeMillis();
//		display.displayText((size*size)+" correlation in "+(endTime-startTime)+" ms");
		return result;
	}
	
	private void correlationresultCsvDisplay(List<CorrelationResult> allresult,String outputFileName) {
		File outputFile = null;
		if ((outputFileName != null) && (outputFileName.length() != 0)) {
			outputFile = initTraceFile(outputFileName);
		}
		if (allresult == null) {
			System.out.println("null result...");
			return;
		}
		if (allresult.isEmpty()) {
			System.out.println("empty result...");
			return;
		}
		trace(outputFile,CorrelationResult.getCsvHeader());
		for (CorrelationResult result:allresult) {
			trace(outputFile,result.toCsv());
		}
	}
	
	private ResultRaw correlate(List<ImageMatrix> data) {		
		if ((data == null) || (data.size() == 0)) {
			System.out.println("data folder must contains png or jpg images");
			return null;
		}
		long startTime = System.currentTimeMillis();
		int size = data.size();
		ResultRaw result = new ResultRaw(size);	
		
		int i = 0;
		int j = 0;
		for (ImageMatrix ref:data) {
			j=0;
			for (ImageMatrix input:data) {
				result.matrix[i][j++] = input.correlate(ref);
			}
			result.header[i] = ref.getName();
			i++;
		}
		long endTime = System.currentTimeMillis();
		display.displayText((size*size)+" correlation in "+(endTime-startTime)+" ms");
		return result;
	}
	
	private void rawDisplay(ResultRaw result,String outputFileName) {
		File outputFile = null;
		if ((outputFileName != null) && (outputFileName.length() != 0)) {
			outputFile = initTraceFile(outputFileName);
		}
		if (result == null) {
			System.out.println("null result...");
			return;
		}
		trace(outputFile,getCsvHeader());
		int sizex = result.matrix.length;
		if (sizex == 0) {
			System.out.println("empty result...");
			return;
		}
		int sizey = result.matrix[0].length;
		for (int i=0; i<sizex; i++) {
			for (int j=0; j<sizey; j++) {
				trace(outputFile,getCsvOutput(result.matrix[i][j], result.header[i]+'/'+result.header[j]));
			}			
		}
	}
	
	public void displayDeepResultAsText(DeepResult result) {
		for (int img=0; img<result.numberOfImages; img++) {
			for (int channel=0; channel<result.numberOfChannel; channel++) {
				ArrayList<DeepResult.Entry> entry = result.sortedResult[img][channel];
				if ((entry != null) && (entry.size() > 2)) {
					DeepResult.Entry second = entry.get(1); // best == 0
					display.displayText(result.imageNames[img]+" : 2nd : "+result.imageNames[second.imageIndex]+" ("+second.score+")");
				}
			}
		}
	}
	
	private void deltaDisplay(ResultRaw result,double[][] delta,String outputFileName) {
		File outputFile = null;
		if ((outputFileName != null) && (outputFileName.length() != 0)) {
			outputFile = initTraceFile(outputFileName);
		}
		if (result == null) {
			System.out.println("null result...");
			return;
		}
		trace(outputFile,getCsvHeader());
		int sizex = result.matrix.length;
		if (sizex == 0) {
			System.out.println("empty result...");
			return;
		}
		for (int i=0; i<sizex; i++) {
			trace(outputFile,getCsvOutput(delta[i], result.header[i]));						
		}
	}
	
	
	
	/**
	 * calculate delta between best correlation score and second one on the different layers
	 * @param result
	 * @return
	 */
	private double[][] deltaCorrelation(ResultRaw result) {
		int sizex = result.matrix.length;
		int sizey = result.matrix[0].length;
		double[][] delta = new double[sizex][AbstractImage.PIXEL_INFO_SIZE];
		for (int i=0; i<sizex; i++) {
			int [] best = new int[AbstractImage.PIXEL_INFO_SIZE]; // best correlation index
			int [] next = new int[AbstractImage.PIXEL_INFO_SIZE]; // second correlation index
			for (int k=AbstractImage.PIXEL_INFO_FIRST; k<AbstractImage.PIXEL_INFO_SIZE; k++) {
				best[k] = 0;
				for (int j=1; j<sizey; j++) { // start at 1 because pre-init on 0
					if (result.matrix[i][j][k] > result.matrix[i][best[k]][k]) {
						best[k] = j;
					}
				}
				next[k] = -1;
				for (int j=0; j<sizey; j++) {
					if (j != best[k]) {
						if (next[k] == -1) { // first one
							next[k] = j;
						} else {
							if (result.matrix[i][j][k] > result.matrix[i][next[k]][k]) {
								next[k] = j;
							}
						}
					}
				}
				delta[i][k] = result.matrix[i][best[k]][k] - result.matrix[i][next[k]][k];
			}
		}
		return delta;
	}
	
	private DeepResult analyze(ResultRaw rawResult) {		
		DeepResult deepResult = new DeepResult(rawResult.size,AbstractImage.PIXEL_INFO_SIZE,dataFolderName,rawResult.header);
		Comparator<DeepResult.Entry> entryComparator = new Comparator<DeepResult.Entry>() {
			@Override
			public int compare(DeepResult.Entry lhs, DeepResult.Entry rhs) {
				if (lhs.score > rhs.score) {
					return -1;
				}
				if (lhs.score == rhs.score) {
					return 0;
				}
				return 1;
			}
			
		};
		for (int i=0; i<deepResult.numberOfImages; i++) {
			for (int k=AbstractImage.PIXEL_INFO_FIRST; k<deepResult.numberOfChannel; k++) {
				ArrayList<DeepResult.Entry> entry = new ArrayList<DeepResult.Entry>();				
				for (int j=0; j<deepResult.numberOfImages; j++) {
					entry.add(deepResult.new Entry(j,rawResult.matrix[i][j][k]));
				}
				Collections.sort(entry,entryComparator);
				deepResult.sortedResult[i][k] = entry;
			}
		}
		return deepResult;
	}
	
	private String getCsvHeader() {
		StringBuilder sb = new StringBuilder();
		sb.append("name");
		for (int i=AbstractImage.PIXEL_INFO_FIRST; i<AbstractImage.PIXEL_INFO_SIZE; i++) {
			sb.append(',');
			sb.append(AbstractImage.pixelImage(i));
		}
		return sb.toString();
	}
	
	private String getCsvOutput(double[] correlation,String header) {
		StringBuilder sb = new StringBuilder();
		sb.append(header);
		for (int i=AbstractImage.PIXEL_INFO_FIRST; i<AbstractImage.PIXEL_INFO_SIZE; i++) {
			sb.append(",");
			sb.append(correlation[i]);
		}
		return sb.toString();
	}

	private void trace(File file,String msg) {		
		if (file == null) {
			System.out.println(msg);
			return;
		}
		try {
			Writer output = new BufferedWriter(new FileWriter(file, true));
			output.append(msg);
			output.append('\n');
			output.close();
		} catch (IOException e) {
		}
	}
	
	private File initTraceFile(String outputFileName) {
		File traceFile = new File(outputFileName); 
		if (traceFile.exists()) {
			traceFile.delete();
		}
		return traceFile;
	}
}
