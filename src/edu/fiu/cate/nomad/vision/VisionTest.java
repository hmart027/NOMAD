package edu.fiu.cate.nomad.vision;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.opencv.calib3d.StereoSGBM;
import org.opencv.core.Core;
import org.opencv.core.CvType;
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

import com.martindynamics.py4j.test.ImageObject;
import com.martindynamics.py4j.test.ObjectDetectionResults;
import com.martindynamics.video.VideoTools;
import com.sun.xml.internal.bind.v2.schemagen.xmlschema.List;

import edu.fiu.cate.nomad.gui.binocular.StereoView;
import edu.fiu.cate.nomad.vision.NOMADVideoClient;
import image.tools.ITools;

public class VisionTest extends Thread implements ObjectDetectorListener{
	
	private String ip = "192.168.1.198";
	
	PID rPID = new PID(0.7, 0.15, 0.01);
	PID lPID = new PID(0.7, 0.15, 0.01);
	
	float psEyeHorizontalFOV = 75;
	float psEyeVerticalFOV = psEyeHorizontalFOV*3f/4f;
	
	int frameW, frameH, frameXC, frameYC;
	double xFovScale, yFovScale;

	boolean initialized = false;
	volatile boolean newLeftFrame = false;
	volatile boolean newRightFrame = false;
	volatile Mat frame_left, frame_right;
	volatile BufferedImage imgL, imgR;
	byte[][][] imgArray;
	CascadeClassifier faceClass, eyeClass;
	StereoSGBM stereoBM;
	
	float cameraX = 0, cameraY = 0;
	double x, y, z;

	ArrayList<FaceEntry> faceList = new ArrayList<>();
	volatile ObjectDetectionResults objRes = null;
	
	StereoView view;
	ServerClient controller;
	BinocularCameraControl eyesController;
	
	ArrayList<String> labels = new ArrayList<>(Arrays.asList(new String[] {"BG", "person", "bicycle", "car", "motorcycle", "airplane",
	                                "bus", "train", "truck", "boat", "traffic light",
	                                "fire hydrant", "stop sign", "parking meter", "bench", "bird",
	                                "cat", "dog", "horse", "sheep", "cow", "elephant", "bear",
	                                "zebra", "giraffe", "backpack", "umbrella", "handbag", "tie",
	                                "suitcase", "frisbee", "skis", "snowboard", "sports ball",
	                                "kite", "baseball bat", "baseball glove", "skateboard",
	                                "surfboard", "tennis racket", "bottle", "wine glass", "cup",
	                                "fork", "knife", "spoon", "bowl", "banana", "apple",
	                                "sandwich", "orange", "broccoli", "carrot", "hot dog", "pizza",
	                                "donut", "cake", "chair", "couch", "potted plant", "bed",
	                                "dining table", "toilet", "tv", "laptop", "mouse", "remote",
	                                "keyboard", "cell phone", "microwave", "oven", "toaster",
	                                "sink", "refrigerator", "book", "clock", "vase", "scissors",
	                                "teddy bear", "hair drier", "toothbrush"}));
		
	static{
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}

	private final Object monitor = new Object();
	
	public VisionTest() {
		//new CameraControl(true);
		// new StereoView();
		
		loadCascadeClassifiers();
		stereoBM = StereoSGBM.create(32, 256, 5);
		
		eyesController = new BinocularCameraControl();

		view = new StereoView();	
		
		ObjectDetector objDetector = new ObjectDetector();
		objDetector.connectToServer("10.102.208.104", 1586);
		objDetector.addObjectDetectorListener(this);
		objDetector.start();

		NOMADVideoClient client = NOMADVideoClient.getInstance(ip, 1553);
		if (client != null) {
			client.addVideoPacketListener(objDetector);
			client.addFrameListener(new FrameListener() {
				@Override
				public void onFrameReceived(FrameEvent e) {
					BufferedImage img = new BufferedImage(e.frame.getWidth(), e.frame.getHeight(),
							BufferedImage.TYPE_3BYTE_BGR);
					byte[] imgData = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
					VideoTools.yuv4202bgr(e.frame.getData(), imgData, e.frame.getHeight(), e.frame.getWidth());
					//System.out.println("frame: "+e.chanel+", "+newRightFrame+", "+newLeftFrame);
					if (e.chanel == 0 && !newRightFrame) {
						frame_right   = bufferedImageToMat(img);
						newRightFrame = true;
					}
					if (e.chanel == 1 && !newLeftFrame) {
						frame_left   = bufferedImageToMat(img);
						newLeftFrame = true;
					}
					synchronized (monitor) {
						monitor.notifyAll();
					}
				}
			});
			client.start();
		}

		controller = new ServerClient();
		controller.start();
	}
	
	@Override
	public void onObjectDetection(ObjectDetectionResults res) {
		objRes = res;
	}
	
	@Override
	public void run() {		
		ArrayList<Scalar> colors = getRandomColors(labels.size());
		for(int i=0; i<labels.size(); i++) {
			Scalar c = colors.get(i);
			System.out.println(labels.get(i)+": "+c.val[0]+", "+c.val[1]+", "+c.val[2]);
		}
		long lT = System.currentTimeMillis();
		while(true) {
			try {
				synchronized (monitor) {
					while (!newRightFrame || !newLeftFrame) {
						monitor.wait();
					}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if(newRightFrame) {
				if(!initialized) {
					frameW = frame_right.width();
					frameH = frame_right.height();
					frameXC = frameW/2;
					frameYC = frameH/2;
					xFovScale = psEyeHorizontalFOV/(double)frameW;
					yFovScale = psEyeVerticalFOV/(double)frameH;
					initialized = true;
				}
				if(objRes!=null)
					for(ImageObject obj: objRes.objects) {
						Scalar color = colors.get(labels.indexOf(obj.label));
						Imgproc.rectangle(frame_right, 
								new Point(obj.coordinates[1], obj.coordinates[0]),  
								new Point(obj.coordinates[3], obj.coordinates[2]), 
								color);
						Imgproc.putText(frame_right, obj.label+": "+obj.score, 
								new Point(obj.coordinates[1], obj.coordinates[2]), 5, 1.0, 
								color);
//						if(obj.label.equals("person")) {
//							double xc = ( (obj.coordinates[1]+obj.coordinates[3])/2 - frameXC)*xFovScale + cameraX;
//							double yc = (-(obj.coordinates[0]+obj.coordinates[2])/2 + frameYC)*yFovScale + cameraY;
//							double w  = (obj.coordinates[1]+obj.coordinates[3])/2*xFovScale;
//							double h  = (obj.coordinates[0]+obj.coordinates[2])/2*yFovScale;
//							double tD = 0.15/Math.tan(Math.toRadians(w/2));
//							eyesController.setFocalPoint(BinocularCameraControl.EYE_RIGHT, cameraX, cameraY, (float)tD);
//						}
					}
				
				view.setRightImage(getBufferedImage(frame_right));
				newRightFrame = false;
				
				Rect[] faces = findFaces(frame_right.clone());
				MatOfRect[] eyes = findEyes(frame_right, faces);
				BufferedImage facesImg = getBufferedImage(frame_right);
				
				long t = System.currentTimeMillis();
				
				if(faces.length>0){
					System.out.println("Faces: "+faces.length);
					for(Rect face: faces){
						double xC = (-frameXC + face.width/2.0  + face.x)*xFovScale + cameraX;
						double yC = (frameYC - face.height/2.0 - face.y)*yFovScale +cameraY;
						double w  = face.width*xFovScale;
						double h  = face.height*yFovScale;
						//System.out.println("\tFace: "+(xC)+", "+(yC));
						FaceEntry faceE = new FaceEntry(xC, yC, w, h);
						if(faceList.contains(faceE)){
							faceE = faceList.get(faceList.indexOf(faceE));
							faceE.x = xC;
							faceE.y = yC;
							faceE.w = w;
							faceE.h = h;
							faceE.lastSeen = t;
						}else{
							faceE.firstSeen = t;
							faceE.lastSeen = t;
							faceList.add(faceE);
						}
					}
					Collections.sort(faceList);
				}
	
				double tX, tY, tD;
				for (int i = 0; i < faceList.size(); i++) {
					FaceEntry f = faceList.get(i);
					if ((t - f.lastSeen) > 3000) { // remove face If not seen in last 1 seconds.
						faceList.remove(i);
						i--;
					} else {
						facesImg = getBufferedImage(drawFace(frame_right, f, i));
						tX = f.x;
						tY = f.y;
						System.out.println("\tFace: " + Math.round(tX) + ", " + Math.round(tY) + ", area: " +
						Math.round(f.w*f.h)+ ", age: "	+ (f.lastSeen - f.firstSeen));
					}
				}
				System.out.println("   Left: " + faceList.size());
	
				if (faceList.size() > 0) {
					tX = faceList.get(0).x;
					tY = faceList.get(0).y;
					tD = 0.15/Math.tan(Math.toRadians(faceList.get(0).w/2));
				} else {
					tX = 0;
					tY = 0;
					tD = 100;
				}
	
				t = System.currentTimeMillis();
				cameraX = (float) rPID.run(tX - cameraX, (t - lT) / 1000f);
				cameraY = (float) lPID.run(tY - cameraY, (t - lT) / 1000f);
				lT = t;
				if (cameraX > 90)
					cameraX = 90;
				if (cameraX < -90)
					cameraX = -90;
				if (cameraY > 90)
					cameraY = 90;
				if (cameraY < -90)
					cameraY = -90;
				System.out.println("\tCam:  " + cameraX + ", " + cameraY);
				eyesController.setFocalPoint(BinocularCameraControl.EYE_RIGHT, cameraX, cameraY, (float)tD);
				controller.sendObject(eyesController.getMessage());
				
//				view.setRightImage(facesImg);
			}
			if(newLeftFrame) {
				view.setLeftImage(getBufferedImage(frame_left));
				newLeftFrame = false;
			}
		}
	}
	
	public ArrayList<Scalar> getRandomColors(int N){
		ArrayList<Scalar> colors = new ArrayList<>();
		double d = 360d/(double)N;
		for(int i=0; i<N; i++) {
			int[] rgb = getRGBfromHSV(i*d, 1, 1);
			colors.add(new Scalar(rgb[0], rgb[1], rgb[2]));
		}
		//Collections.shuffle(colors);
		return colors;
	}
	
	public int[] getRGBfromHSV(double h, double s, double v) {
		double c = v*s;
		double x = c*(1-Math.abs((h/60d)%2-1));
		double m = v-c;
		double rp=0, gp=0, bp=0;
		if(0<=h && h<60) {
			rp = c;
			gp = x;
			bp = 0;
		}else if(60<=h && h<120) {
			rp = x;
			gp = c;
			bp = 0;
		}else if(120<=h && h<180) {
			rp = 0;
			gp = c;
			bp = x;
		}else if(180<=h && h<240) {
			rp = 0;
			gp = x;
			bp = c;
		}else if(240<=h && h<300) {
			rp = x;
			gp = 0;
			bp = c;
		}else if(300<=h && h<360) {
			rp = c;
			gp = 0;
			bp = x;
		}
		return new int[] {(int)((rp+m)*255),(int)((gp+m)*255),(int)((bp+m)*255)};
	}
	
	public static Mat bufferedImageToMat(BufferedImage bi) {
		Mat mat = new Mat(bi.getHeight(), bi.getWidth(), CvType.CV_8UC3);
		byte[] data = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
		mat.put(0, 0, data);
		return mat;
	}
	
	public float[] getAngularDelta(int x, int y, int w, int h){
		return new float[]{(w-x*2)/(float)w*psEyeHorizontalFOV/2f, (h-y*2)/(float)h*psEyeVerticalFOV/2f};
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
	
	public BufferedImage get16BitGrayBufferedImage(Mat img){
		// Create an empty image in matching format
		BufferedImage out = new BufferedImage(img.width(), img.height(), BufferedImage.TYPE_BYTE_GRAY);
		// Get the BufferedImage's backing array and copy the pixels directly into it
		byte[] data = ((DataBufferByte) out.getRaster().getDataBuffer()).getData();
		//img.get(0, 0, data)
		short[] dataT = new short[img.width()*img.height()];
		img.get(0, 0, dataT);
		for(int i=0; i<dataT.length; i++){
			data[i] = (byte) (dataT[i]/16);
		}
		return out;
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
	
	public Mat drawFace(Mat img, FaceEntry face, int ID){
		
		int x, y, w, h;
		w = (int)Math.round(face.w/xFovScale);
		h = (int)Math.round(face.h/yFovScale);
		
		x = (int) ((face.x-cameraX)/xFovScale + frameXC - w/2.0);
		y = (int) (-(face.y-cameraY)/yFovScale + frameYC - h/2.0);
		
		Scalar color = new Scalar(0,0,255);
				
		Imgproc.rectangle(img, new Point(x, y),  new Point(x+w, y+h), color);
		Imgproc.putText(img, "id: "+ID, new Point(x,y), 5, 1.0, color);
		
		return img;
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
	
    public class FaceEntry implements Comparable<FaceEntry>{
		
		double minAreaChange = 0.6;
		double maxAreaChange = 1.0/minAreaChange;
		double maxDistance   = 10;
		
		double x, y, w, h;
		long firstSeen, lastSeen;
		
		public FaceEntry(double x, double y, double w, double h){
			this.x = x;
			this.y = y;
			this.w = w;
			this.h = h;
		}

		public boolean equals(Object o){
			if(o==null)
				return false;
			if(!this.getClass().equals(o.getClass()))
				return false;
			FaceEntry f = (FaceEntry) o;
			double distance  = Math.sqrt((x-f.x)*(x-f.x) + (y-f.y)*(y-f.y));
			double areaChange = (double)(w*h)/(double)(f.w*f.h);
			if(areaChange>=minAreaChange && areaChange<=maxAreaChange && distance<=maxDistance)
				return true;
			return false;
		}
		
		@Override
		public int compareTo(FaceEntry o) {
			if(this.equals(o))
				return 0;
//			if((w*h)<(o.w*o.h)){
//				return -1;
//			}else if ((w*h)>(o.w*o.h)){
//				return 1;
//			}else{
//				float d1 = (float) Math.sqrt(x*x + y*y);
//				float d2 = (float) Math.sqrt(o.x*o.x + o.y*o.y);
//				if(d1<d2)
//					return -1;
//				if(d1>d2)
//					return 1;
//			}
//			return 0;
			if(this.firstSeen<o.firstSeen)
				return -1;
			else
				return 1;
		}
		
	}
	
	public class PID {
		double kP,kI, kD;
		double lE = 0, iE = 0, dE = 0;
		
		public PID(double kP, double kI, double kD){
			this.kP = kP;
			this.kI = kI;
			this.kD = kD;
		}
		
		public double run(double error, double dt){
			dE = (error-lE)/dt;
			iE += error*dt;
			lE = error;
			return kP*error + kI*iE + kD*dE;
		}
		
	}
	
	public class ServerClient extends Thread{

		private int port = 5050;
		private Socket sock;
		private ObjectInputStream inStream;
		private ObjectOutputStream outStream;
		
		public ServerClient() {}
		
		public synchronized boolean sendObject(Object o) {
			if(outStream!=null) {
				try {
					outStream.reset();
					outStream.writeObject(o);
					outStream.flush();
					return true;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return false;
		}
		
		@Override
		public void run() {

			try {
				System.out.println("Attempting server connection to "+ip+": "+port);
				sock = new Socket(ip, port);
				System.out.println("Server client connected!");
				outStream = new ObjectOutputStream(sock.getOutputStream());
				inStream  = new ObjectInputStream(sock.getInputStream());
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				if(e.getMessage().contains("Connection refused")) {
					System.out.println("Connection refused");
				}else {
					e.printStackTrace();
				}
			}
			
			while(true){
				if(inStream==null || outStream==null){
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}else{
					try {
						Object obj = null;
						synchronized (inStream) {
							obj = inStream.readObject();
						}
						
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					} catch(StreamCorruptedException e) {
						System.out.println("Called defaultReadObject");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		
	}

	public static void main(String[] args) {
		new VisionTest().start();
	}
}
