package edu.fiu.cate.nomad.vision;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import com.martindynamics.py4j.test.ObjectDetectionResults;
import com.martindynamics.video.stream.VideoPacket;

public class ObjectDetector extends Thread implements VideoPacketListener{

	private String ip = "";
	private int port = 1586;
	private Socket sock;
	private ObjectInputStream inStream;
	private ObjectOutputStream outStream;
	
	public ArrayList<ObjectDetectorListener> listeners = new ArrayList<>();
	
	public synchronized boolean connectToServer(String ip, int port) {
		this.ip   = ip;
		this.port = port;
		return attemptConnection();
	}
	
	public synchronized boolean attemptConnection() {
		try {
			System.out.println("Attempting ObjectDetector connection to "+ip+": "+port);
			sock = new Socket(ip, port);
			System.out.println("ObjectDetector connected!");
			outStream = new ObjectOutputStream(sock.getOutputStream());
			inStream  = new ObjectInputStream(sock.getInputStream());
			return true;
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			if(e.getMessage().contains("Connection refused")) {
				System.out.println("ObjectDetector Connection refused");
			}else {
				e.printStackTrace();
			}
		}
		return false;
	}
	
	public synchronized void disconnect() {
		if(sock!=null) {
			try {
				if(!sock.isClosed()) {
					sock.shutdownOutput();
					sock.shutdownInput();
					inStream.close();
					outStream.close();
					sock.close();
				}
				inStream  = null;
				outStream = null;
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	public synchronized boolean sendVideoPacket(VideoPacket pkt) {
		if(outStream!=null){
			synchronized (outStream) {
				try {
					outStream.reset();
					outStream.writeObject(pkt);
					outStream.flush();
					return true;
				} catch (IOException e) {
					if(e.getClass().equals(SocketException.class)) {
						disconnect();
					}else {
						e.printStackTrace();
					}
				}
			}
		}
		return false;
	}
	
	public void addObjectDetectorListener(ObjectDetectorListener l) {
		listeners.add(l);
	}
	
	public void run() {
		while(true){
			if(inStream==null || outStream==null){
				if(!attemptConnection()) {
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}else{
				try {
					Object obj = null;
					synchronized (inStream) {
						obj = inStream.readObject();
					}
					if(obj!=null) {
						if(obj.getClass().equals(ObjectDetectionResults.class)) {
							for(ObjectDetectorListener l: listeners)
								l.onObjectDetection((ObjectDetectionResults)obj);
						}else {
							System.out.println("ObjectDetector received object: "+obj.getClass());
						}
					}
					
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch(StreamCorruptedException e) {
					System.out.println("ObjectDetector stream corrupted.");
				} catch (IOException e) {
					disconnect();
				}
			}
		}
	}

	
	@Override
	public void onPacketReceived(VideoPacket pck) {
		if(pck.channel==0)
			sendVideoPacket(pck);
	}
}
