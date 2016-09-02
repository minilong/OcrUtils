package ocr.processor.global;

import java.io.File;

import ocr.processor.logic.ImagePreprocess;

import org.junit.Before;
import org.junit.Test;

public class ImageThresholdTest {

	private ImagePreprocess thres;
	
	@Before
	public void setup(){
		thres = new ImagePreprocess();
	}
	
	@Test
	public void testThreshold(){
		thres.process("sample/xiaolong-bei.jpg");
	}
	
	@Test
	public void testListFiles(){
		File dir = new File("sample");
		for(File f : dir.listFiles()){
			thres.process(f.getAbsolutePath());
		}
	}
}
