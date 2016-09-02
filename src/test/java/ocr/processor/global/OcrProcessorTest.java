package ocr.processor.global;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import ocr.processor.OcrProcessor;

public class OcrProcessorTest {

	private OcrProcessor ocr;
	
	@Before
	public void setup(){
		this.ocr = new OcrProcessor();
	}
	
	@Test
	public void testDect() throws FileNotFoundException, IOException{
		File dir = new File("sample");
		for(File f : dir.listFiles()){
			this.ocr.detectDuetime(new FileInputStream(f));
		}
	}
}
