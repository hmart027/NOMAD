package edu.fiu.cate.nomad.video.streaming;

import image.tools.ITools;
import image.tools.IViewer;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

import org.opencv.calib3d.StereoSGBM;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IContainerFormat;
import com.xuggle.xuggler.IPixelFormat;
import com.xuggle.xuggler.io.XugglerIO;
import com.xuggle.xuggler.video.ConverterFactory;
import com.xuggle.xuggler.video.IConverter;

import edu.fiu.cate.nomad.gui.binocular.StereoView;

public class VideoServer extends Thread{
	
	int leftCameraIndex = 1;
	int rightCameraIndex = 2;
	
	OutputStream outputStream;
	boolean rCamLoaded, lCamLoaded;
	VideoCapture camera_left;
	VideoCapture camera_right;
	Mat frame_left;
	Mat frame_right;
	BufferedImage imgL;
	BufferedImage imgR;
	byte[][][] imgArray;
	
	IMediaWriter writer;
	IConverter buf2Img;
	
	CascadeClassifier faceClass;
	CascadeClassifier eyeClass;
	StereoSGBM stereoBM; 
	
	StereoView gui = new StereoView();
	IViewer dispView = new IViewer("", null);
	
	volatile boolean writterReady = false;
	volatile boolean grabFrames = false;
	volatile boolean grabbed = false;
		
	static{
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}
	
	public VideoServer(){	
//		try {
//			outputStream = new DataOutputStream(new FileOutputStream("/home/harold/Videos/mv.h264"));
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		
		loadCascadeClassifiers();
		
		stereoBM = StereoSGBM.create(32, 256, 5);
		
		new ClientListener().start();
				
		camera_left = new VideoCapture(leftCameraIndex);
		frame_left = new Mat();
		if (!camera_left.isOpened()) {
			System.out.println("Left Camera Error");
		} else {
			System.out.println("Left Camera OK");
			lCamLoaded = true;
		}
		camera_right = new VideoCapture(rightCameraIndex);
		frame_right = new Mat();
		if (!camera_right.isOpened()) {
			System.out.println("Right Camera Error");
		} else {
			System.out.println("Right Camera OK");
			rCamLoaded = true;
		}
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		if(lCamLoaded){
			camera_left.read(frame_left);
		}
		if(rCamLoaded){
			camera_right.read(frame_right);
		}else{
			frame_right=frame_left;
		}
		this.imgL = getBufferedImage(frame_left);
		this.imgR = getBufferedImage(frame_right);
		this.imgArray = getImageArray(frame_left);
		
		System.out.println(frame_left.width() + "x" + frame_left.height());
		System.out.println(camera_left.get(Videoio.CAP_PROP_FPS) + " fps");
				
	}
	
	public void run(){
		new FrameGrabber().start();
		
		long t0 = System.nanoTime();
		while (true) {
			grabFrames = true;
			while(!grabbed){
				try {
					Thread.sleep(0, 100000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			};
			grabFrames = false;
			grabbed = false;
			
			if(writterReady){
				writer.encodeVideo(0, this.imgL, System.nanoTime()-t0, TimeUnit.NANOSECONDS);
			}
			Rect[] faces = findFaces(frame_left.clone());
			MatOfRect[] eyes = findEyes(frame_left, faces);
			BufferedImage facesImg = getBufferedImage(drawFaces(frame_left, faces, eyes));
			
//			Mat disp = new Mat();
//			Mat frame_left_gray = new Mat();
//			Mat frame_right_gray = new Mat();
//			Imgproc.cvtColor(frame_left, frame_left_gray, Imgproc.COLOR_BGR2GRAY);
//			Imgproc.cvtColor(frame_right, frame_right_gray, Imgproc.COLOR_BGR2GRAY);
//			stereoBM.compute(frame_left_gray, frame_right_gray, disp);
//			Mat dispImg = new Mat();
//			disp.convertTo(dispImg, CvType.CV_8UC1, 1.0 / 16.0);
////			stereoBM.compute(frame_right_gray, frame_left_gray, disp);
//			dispView.setImage(getGrayBufferedImage(dispImg));
			
//			if(faces.length>0){
//				Mat face = new Mat(frame, faces[0]);
//				v2.setImage(getBufferedImage(face));
//			}
			
//			v.setImage(this.img);
			gui.setLeftImage(imgR);
			gui.setRightImage(facesImg);
//			gui.setRightImage(imgR);
			getImageArray(frame_left, imgArray);
			
//			v2.setImage(ImageManipulation.getBufferedImage(getEdges(imgArray)));
		}
	}
	
	public byte[][][] getImageArray(Mat img){
		int width = img.width();
		int height = img.height();
		int channels = img.channels();
		int w=0, h=0;
		byte[] data = new byte[width*height*channels];
		byte[][][] out = new byte[channels][height][width];
		img.get(0, 0, data);
		for(int i=0; i<data.length; i+=3){
			out[2][h][w] = data[i];
			out[1][h][w] = data[i+1];
			out[0][h][w] = data[i+2];
			w++;
			if(w==width){
				w=0;
				h++;
			}
		}
		return out;
	}
	
	public void getImageArray(Mat img, byte[][][] dst){
		int width = img.width();
		int height = img.height();
		int channels = img.channels();
		int w=0, h=0;
		byte[] data = new byte[width*height*channels];
		img.get(0, 0, data);
		for(int i=0; i<data.length; i+=3){
			dst[2][h][w] = data[i];
			dst[1][h][w] = data[i+1];
			dst[0][h][w] = data[i+2];
			w++;
			if(w==width){
				w=0;
				h++;
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
	
	public BufferedImage getGrayBufferedImage(Mat img){
		// Create an empty image in matching format
		BufferedImage out = new BufferedImage(img.width(), img.height(), BufferedImage.TYPE_BYTE_GRAY);
		// Get the BufferedImage's backing array and copy the pixels directly into it
		byte[] data = ((DataBufferByte) out.getRaster().getDataBuffer()).getData();
		img.get(0, 0, data);
		return out;
	}
	
	private class ClientListener extends Thread{
		public volatile boolean running;
		Socket client;
		
		public ClientListener(){
			running = true;
		}
		
		public void run(){
			while(running){
				if(client == null || client.isClosed()){
					try {
						writterReady = false;
						System.out.println("Waiting for Client");
						ServerSocket server = new ServerSocket(5050);
						client = server.accept();
						outputStream = client.getOutputStream();
						System.out.println("Client Connected");
						server.close();
						setVideoWriter();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	public void setVideoWriter(){
		writer = ToolFactory.makeWriter(XugglerIO.map(outputStream));
		// manually set the container format (because it can't detect it by filename anymore)
		IContainerFormat containerFormat = IContainerFormat.make();
		System.out.println(containerFormat.setOutputFormat("h264", null, "h264"));
		writer.getContainer().setFormat(containerFormat);

		// add the video stream
		System.out.println(writer.addVideoStream(0, 0, ICodec.ID.CODEC_ID_H264, frame_left.width(), frame_left.height()));
		
		if(buf2Img == null){
			buf2Img = ConverterFactory.createConverter(this.imgL, IPixelFormat.Type.BGR24);
		}
		
		writterReady = true;
	}

	public void loadCascadeClassifiers(){
		faceClass = new CascadeClassifier();
		eyeClass = new CascadeClassifier();
		
//		System.out.println(faceClass.load("/home/harold/OpenCV/data/haarcascades/haarcascade_frontalface_alt2.xml"));
//		System.out.println(eyeClass.load("/home/harold/OpenCV/data/haarcascades/haarcascade_eye.xml"));
		
		String jarPath = System.getProperty("user.dir");
		System.out.println("Loading classifiers from: "+jarPath+"/haarcascades/");
		
		System.out.println("Face classifier loaded?: "+faceClass.load(jarPath+"/haarcascades/haarcascade_frontalface_alt2.xml"));
		System.out.println("Eye classifier loaded?:  "+eyeClass.load(jarPath+"/haarcascades/haarcascade_eye.xml"));
	}

	public Rect[] findFaces(Mat img){
		Mat gray = new Mat();
		MatOfRect faces = new MatOfRect();
		MatOfInt rLevels = new MatOfInt();
		MatOfDouble weights = new MatOfDouble();
		
		//Image to gray
		Imgproc.cvtColor(img, gray, Imgproc.COLOR_RGB2GRAY);
		
//		faceClass.detectMultiScale3(gray, faces, rLevels, weights, 1.1, 3, 0, new Size(), new Size(), true);//broken, takes too long
//		faceClass.detectMultiScale(gray, faces);
		faceClass.detectMultiScale(gray, faces, 1.1, 3, 0, new Size(), new Size());
				
		return faces.toArray();
	}
	
	public MatOfRect[] findEyes(Mat img, Rect[] fs){
		MatOfRect[] out = new MatOfRect[fs.length];
		Mat gray = new Mat();
		//Image to gray
		Imgproc.cvtColor(img, gray, Imgproc.COLOR_RGB2GRAY);
		
		for(int f=0; f<fs.length; f++){
			MatOfRect eyes = new MatOfRect();
			eyeClass.detectMultiScale(new Mat(gray, fs[f]), eyes);
			out[f] = eyes;
		}
	
		return out;
	}
	
	public Mat drawFaces(Mat img, Rect[] fs, MatOfRect[] eyes){
		Mat out = img.clone();
		
		for(int i=0; i<fs.length; i++){
			Imgproc.rectangle(out, new Point(fs[i].x, fs[i].y),  new Point(fs[i].x+fs[i].width, fs[i].y+fs[i].height), new Scalar(0,0,255));
			Rect[] e = eyes[i].toArray();
			for(int j=0; j<e.length; j++){
				Imgproc.rectangle(out, new Point(fs[i].x+e[j].x, fs[i].y+e[j].y),  new Point(fs[i].x + e[j].x+e[j].width, fs[i].y + e[j].y+e[j].height), new Scalar(255,0,0));
			}
		}
	
		return out;
	}

	public byte[][][] getEdges(byte[][][] img){
		float[][][] tImg = new float[img.length][][];
		for(int c=0; c<img.length; c++){
			tImg[c] = ITools.getLaplacianMag(ITools.byte2Float(img[c]));
		}
		return ITools.normilize(tImg);
	}
	 
	public void getHDisparity(byte[][][] img, int kSize, int min, int max){
		
	}

	public class FrameGrabber extends Thread{
		
		public void run(){
			Mat tr = new Mat();
			Mat tl = new Mat();
			while (true){
				if(lCamLoaded){
					camera_left.read(tl);
				}
				if(rCamLoaded){
					camera_right.read(tr);
				}else{
					tr=tl;
				}
				if(grabFrames && !grabbed){
					frame_left = tl.clone();
					frame_right = tr.clone();
					imgL = getBufferedImage(frame_left);
					imgR = getBufferedImage(frame_right);
					grabbed = true;
				}
			}
		}
	}
}
