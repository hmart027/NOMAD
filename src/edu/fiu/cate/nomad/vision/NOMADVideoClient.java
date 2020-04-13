package edu.fiu.cate.nomad.vision;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import org.opencv.core.Core;

import com.martindynamics.video.stream.VideoPacket;
import com.martindynamics.video.stream.VideoStream;
import com.martindynamics.video.stream.codec.VideoFrame;

public class NOMADVideoClient extends Thread {
	
	private String ip = "";
	private int port = 1553;
	private Socket sock;
	private ObjectInputStream inStream;
	private ObjectOutputStream outStream;
	
	private VideoStream videoStream;
	
	private ArrayList<FrameListener> listeners = new ArrayList<>();
	
	private NOMADVideoClient(String ip, int port) {
		this.ip   = ip;
		this.port = port;
		videoStream = VideoStream.createVideoStream();
	}
	
	public static NOMADVideoClient getInstance(String ip, int port) {
		try {
			System.loadLibrary(Core.NATIVE_LIBRARY_NAME); 
			System.loadLibrary("VideoStreaming");
		} catch (UnsatisfiedLinkError | SecurityException e) {
			System.out.println("unable to load VideoStreaming");
			e.printStackTrace();
			return null;
		}
		return new NOMADVideoClient( ip, port);
	}
	
	public synchronized boolean connectToServer(String ip, int port) {
		this.ip   = ip;
		this.port = port;
		return attemptConnection();
	}
	
	public synchronized boolean attemptConnection() {
		try {
			System.out.println("Attempting video connection to "+ip+": "+port);
			sock = new Socket(ip, port);
			System.out.println("Video client connected!");
			outStream = new ObjectOutputStream(sock.getOutputStream());
			inStream  = new ObjectInputStream(sock.getInputStream());
			return true;
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			if(e.getMessage().contains("Connection refused")) {
				System.out.println("Connection refused");
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
	
	public void addFrameListener(FrameListener l) {
		this.listeners.add(l);
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
					if(obj.getClass().equals(VideoPacket.class)){
						VideoPacket pkt = (VideoPacket) obj;
						VideoFrame frame = videoStream.decodepacket(pkt);
						if(frame!=null) {
							for(FrameListener f: listeners) {
								f.onFrameReceived(new FrameEvent(pkt.channel, frame));
							}
						}
					}else {
						System.out.println(obj.getClass());
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
