package edu.fiu.cate.nomad.video.streaming;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

import image.tools.IViewer;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;

public class XuggleTrialMain {
	
	static{
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}
	
	XuggleTrialMain(){
		
		VideoCapture camera = new VideoCapture(0);
		Mat frame = new Mat();
		if (!camera.isOpened()) {
			System.out.println("Camera Error");
		} else {
			System.out.println("Camera OK?");
		}

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		camera.read(frame);
		System.out.println("Frame Obtained");
		
		MatOfByte vid = new MatOfByte();
		Imgcodecs.imencode(".jpg", frame, vid);

		System.out.println(frame.size().height*frame.size().width);
		System.out.println(vid.size().height*vid.size().width);
		
		IViewer v = null;
		
		while (true) {
			camera.read(frame);
			if( v == null){
				v = new IViewer("Server", getBufferedImage(frame));
			}else{
				v.setImage(getBufferedImage(frame));
			}
		}
		
	}
	
	public BufferedImage getBufferedImage(Mat img){
		// Create an empty image in matching format
		BufferedImage out = new BufferedImage(img.width(), img.height(), BufferedImage.TYPE_3BYTE_BGR);

		// Get the BufferedImage's backing array and copy the pixels directly into it
		byte[] data = ((DataBufferByte) out.getRaster().getDataBuffer()).getData();
		img.get(0, 0, data);
		return out;
	}

	public static void main(String[] args) {
		new XuggleTrialMain();
	}

}
