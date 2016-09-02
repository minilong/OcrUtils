package ocr.processor.global;

import java.awt.image.BufferedImage;
import java.io.File;

import ocr.processor.logic.ImagePreprocess;
import ocr.processor.logic.OcrDetection;

import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.junit.Before;
import org.junit.Test;

public class OcrDetectionTest {

	private OcrDetection ocr;
	
	private ImagePreprocess thres;

	@Before
	public void setup() {
		this.ocr = new OcrDetection();
		this.thres = new ImagePreprocess();
	}

	@Test
	public void testDetect() {
		this.ocr.detectFile("result/threshold_xiaolong-bei.jpg");
	}

	@Test
	public void testDectectFiles() {
		File sourceDir = new File("sample");
		for (File f : sourceDir.listFiles()) {
			IplImage img = thres.process(f.getAbsolutePath());
			BufferedImage buffImg = thres.convertIpl2Buffer(img);
			String result = ocr.dectectBufferImage(buffImg);
//			String result = ocr.dectectBuffurByte(img.asByteBuffer(), img.width(), img.height());
			System.out.println(f.getName()+":"+result);
			opencv_core.cvRelease(img);
		}
//		File resultDir = new File("result");
//		for(File f2 : resultDir.listFiles()) {
//			String result = this.ocr.detectFile(f2.getAbsolutePath());
//			System.out.println(f2.getName()+":"+result);
//		}
	}
}
