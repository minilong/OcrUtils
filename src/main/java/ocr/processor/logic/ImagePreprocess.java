package ocr.processor.logic;

import static org.bytedeco.javacpp.opencv_imgproc.*;
import static org.bytedeco.javacpp.opencv_imgcodecs.*;
import static org.bytedeco.javacpp.opencv_core.*;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.helper.opencv_core.CvArr;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ImagePreprocess {
	private final Logger logger = LoggerFactory.getLogger(ImagePreprocess.class);
	private final int HIST_MAX_VALUE = 255;
	private final int BLOCK_SIZE = 25;
	private final int SMOOTH_RECURISE = 3;
//	private final int BOTTOM_SIZE = 100;
	
	public IplImage process(String filePath) {
		logger.info(filePath);
		IplImage srcImg = cvLoadImage(filePath);
		return this.processThreshold(srcImg);
	}
	
	public IplImage processThreshold(IplImage srcImg) {
		IplImage grayImg = gray(srcImg);
		IplImage dstImg = cvCreateImage(cvSize(800, 600), IPL_DEPTH_8U, 1);
		try {
			cvResize(grayImg, dstImg);
			CvHistogram tempHist = createGrayImageHist(dstImg);
			double[] sset = smoothHist(tempHist);
			int threshold = findBottom(sset);
			cvThreshold(dstImg, dstImg, threshold, HIST_MAX_VALUE, THRESH_BINARY);
		} finally {
			cvReleaseImage(grayImg);
		}
		return dstImg;
	}
	
	private IplImage grabContours(IplImage origImag){
		CvMemStorage cvMem = cvCreateMemStorage();
		CvSeq seq = new CvSeq();
		cvFindContours(origImag, cvMem, seq);
		IplImage imgCounters = cvCreateImage(cvSize(origImag.width(), origImag.height()), IPL_DEPTH_8U, 3); 
		do {
			CvRect crect = cvBoundingRect(seq);
			cvDrawRect(imgCounters, cvPoint(crect.x(),crect.y()), cvPoint(crect.x() + crect.width(), crect.y() + crect.height()), CV_RGB(255, 255, 255), 2, 2, 0);
		} while ((seq=seq.h_next())!=null);
		return imgCounters;
	}
	
	public BufferedImage convertIpl2Buffer(IplImage grayImg) {
		BufferedImage bfimage = null;
		try {
			OpenCVFrameConverter.ToIplImage grabberConverter = new OpenCVFrameConverter.ToIplImage();
			Java2DFrameConverter paintConverter = new Java2DFrameConverter();
			Frame frame = grabberConverter.convert(grayImg);
			bfimage = paintConverter.getBufferedImage(frame, 1);
		} finally {
			cvReleaseImage(grayImg);
		}
		return bfimage;
	}
	
	
	//获取灰度直方图
	private void getHistValues(CvHistogram hist){
		CvArr tempArr = hist.bins();
		double sum = 0;
		double tsum = 0;
		for(int i=0;i<HIST_MAX_VALUE;i++){
			double v = cvGetReal1D(tempArr, i);
			sum += v;
			if(i<90){
				tsum += v;
			}
			System.out.println(i+" , "+v);
		}
		System.out.println("sum:"+sum);
		System.out.println("percent:"+tsum/sum+"%");
	}
	
	//创建灰度图像
	private CvHistogram createGrayImageHist(IplImage ppImage)  
	{  
	    int[] nHistSize = {HIST_MAX_VALUE+1};  
	    float fRange[] = {0, HIST_MAX_VALUE};  //灰度级的范围
	    CvHistogram pcvHistogram = cvCreateHist(1, nHistSize, CV_HIST_ARRAY);  
	    cvCalcHist(ppImage, pcvHistogram);
	    return pcvHistogram;  
	}
	
	private int findBottom(double[] hists){
		for(int i=1;i<HIST_MAX_VALUE-1;i++){
			if(hists[i-1]>hists[i] && hists[i]<hists[i+1]){
				return i;
			}
		}
		return 0;
	}
	
	//平滑曲线
	private double[] smoothHist(CvHistogram hist){
		CvArr tempArr = hist.bins();
		double[] sset = new double[HIST_MAX_VALUE];
		for(int i=0;i<255;i++){
			double v = cvGetReal1D(tempArr, i);
			sset[i] = v;
		}
		
		for(int i =0;i<SMOOTH_RECURISE;i++){
			sset = avgHistValues(sset, 0.3);
		}
		
		for(int i=0;i<HIST_MAX_VALUE;i++){
			logger.debug(i+","+sset[i]);
		}
		
		return sset;
	}
	
	//平滑函数
	private double[] avgHistValues(double[] sset, double alpha){
		double[] dset = new double[HIST_MAX_VALUE];
		System.arraycopy(sset, 0, dset, 0, HIST_MAX_VALUE);
		
		for(int i=1;i<255;i++){
			dset[i] = alpha*sset[i]+(1-alpha)*dset[i-1];
		}
		return dset;
	}
	

	
	/*IplImage CreateHisogramImage(int nImageWidth, int nScale, int nImageHeight, CvHistogram pcvHistogram)  
	{  
	    IplImage pHistImage = cvCreateImage(cvSize(nImageWidth * nScale, nImageHeight), IPL_DEPTH_8U, 1);  
	    cvRectangle(pHistImage, cvPoint(0, 0), cvPoint(pHistImage.width(), pHistImage.height()), CV_RGB(255, 255, 255));  
	    //统计直方图中的最大直方块  
	    FloatPointer fMaxHistValue = new FloatPointer(0);
	    cvGetMinMaxHistValue(pcvHistogram, fMaxHistValue, null);  
	    
	    
	    pcvHistogram.bins();
	    //分别将每个直方块的值绘制到图中  
	    int i;  
	    for(i = 0; i < nImageWidth; i++)  
	    {  
	        float fHistValue = cvQueryHistValue_1D //像素为i的直方块大小  
	        int nRealHeight = cvRound((fHistValue / fMaxHistValue.get()) * nImageHeight);  //要绘制的高度  
	        cvRectangle(pHistImage,  
	            cvPoint(i * nScale, nImageHeight - 1),  
	            cvPoint((i + 1) * nScale - 1, nImageHeight - nRealHeight),  
	            cvScalar(i, 0, 0, 0),   
	            CV_FILLED  
	        );   
	    }  
	    return pHistImage;  
	}  */
	
	private IplImage gray(IplImage src) {
		IplImage pImg = cvCreateImage(cvGetSize(src), IPL_DEPTH_8U, 3);
		IplImage pGrayImg = cvCreateImage(cvGetSize(pImg), IPL_DEPTH_8U, 1);
		try {
			
			// flags是转换的模式，可以取0：没有变化；1：垂直翻转，即沿x轴翻转；2：交换红蓝信道；
			cvConvertImage(src, pImg, 2);
			// 将RGB转换成Gray度图			
			cvCvtColor(pImg, pGrayImg, CV_RGB2GRAY);
		} finally {
			cvReleaseImage(pImg);
		}
		return pGrayImg;
	}

}
