package ocr.processor;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import ocr.processor.logic.ImagePreprocess;
import ocr.processor.logic.OcrDetection;

import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter.ToIplImage;

public class OcrProcessor {

	private ImagePreprocess prePrc;
	private OcrDetection ocrDect;
	private String dueTimePattern;
	
	public OcrProcessor(){
		prePrc = new ImagePreprocess();
		ocrDect = new OcrDetection();
		dueTimePattern = "(.*(\\d{4}\\s*\\d{2}\\s*\\d{2})\\s*(\\d{4}\\s*\\d{2}\\s*\\d{2}))";
	}
	
	public String detectDuetime(InputStream ins) throws IOException{
		IplImage srcImg = this.convertInputStream(ins);
		//图像灰度直方图阈值提取
		IplImage thImg = prePrc.processThreshold(srcImg);
		//转换图像对象
		BufferedImage buffImg = prePrc.convertIpl2Buffer(thImg);
		//OCR数字识别
		String reStr = ocrDect.dectectBufferImage(buffImg);
		
		String[] times = duetimeStringMatch(reStr);
		
		System.out.println(Arrays.toString(times));
		
		return reStr;
	}
	
	private String[] duetimeStringMatch(String srcStr){
		Scanner scn = new Scanner(srcStr);
		Pattern p = Pattern.compile(dueTimePattern);
		String[] times = null;
		while(scn.hasNext()){
			String line = scn.nextLine();
			Matcher matcher = p.matcher(line);
			if(matcher.matches()){
				times = new String[2];
				times[0] = matcher.group(2);
				times[1] = matcher.group(3);
			}
		}
		return times;
	}
	
	private IplImage convertInputStream(InputStream ins) throws IOException{
		BufferedImage bfimg = ImageIO.read(ins);
		ToIplImage iplConverter = new ToIplImage();
		Java2DFrameConverter java2dConverter = new Java2DFrameConverter();
		IplImage iplImage = iplConverter.convert(java2dConverter.convert(bfimg));
		return iplImage;
	}
	
}
