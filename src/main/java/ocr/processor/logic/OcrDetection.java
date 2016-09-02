package ocr.processor.logic;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import org.bytedeco.javacpp.opencv_core;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

public class OcrDetection {
	private final Logger logger = LoggerFactory.getLogger(OcrDetection.class);
	private static final Tesseract tessInstance = Tesseract.getInstance();
	
	public OcrDetection(){
        tessInstance.setDatapath("./");
        tessInstance.setLanguage("chi_sim");
        tessInstance.setTessVariable("tessedit_char_whitelist", "0123456789");
	}
	
	public String detectFile(String filePath){
		String result = null;
        File imageFile = new File(filePath);
//		instance.setTessVariable("tessedit_char_blacklist", "og丨，,-_′″萼效鳙…害敷嘉〖墩醴");
//		instance.setTessVariable("classify_bln_numeric_mode", "1");
//		instance.doOCR(imageFile, new Rectangle(780, 1295, 1200, 155));
//		instance.setOcrEngineMode(6);
        try {
        	BufferedImage bufferImg = ImageIO.read(imageFile);
        	String temp = this.dectectBufferImage(bufferImg);
        	result = temp.trim();
        } catch (IOException e) {
        	logger.error(e.getMessage(),e);
		}
        return result;
	}
	
	public String dectectBufferImage(BufferedImage bufferImg){
		String result = null;
		 try {
	        	String dres= tessInstance.doOCR(bufferImg);//,new Rectangle(290, 760, 760, 80));//, new Rectangle(780, 1295, 1200, 155));
	        	result = dres.trim();
	            logger.debug(result);
	        } catch (TesseractException e) {
	            logger.error(e.getMessage(),e);
	        }
		return result;
	}
}
