package edu.fiu.cate.nomad.audio;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import edu.fiu.cate.nomad.rasp.subsystems.messages.AudioMessage;

public class NOMADAudioClient extends Thread {
	
	private int audioPort = 1554;
	private String ip = "";
	private Socket sock;
	private ObjectInputStream inStream;
	private ObjectOutputStream outStream;
	
	private ArrayList<AudioFrameListener> listeners = new ArrayList<>();
	
	public NOMADAudioClient(String ip) {
		this.ip   = ip;
	}
	
	public NOMADAudioClient(String ip, int port) {
		this.ip   = ip;
		this.audioPort = port;
	}
	
	public synchronized boolean connectToServer(String ip, int port) {
		this.ip   = ip;
		this.audioPort = port;
		return attemptConnection();
	}
	
	public synchronized boolean attemptConnection() {
		try {
			System.out.println("Attempting audio connection to "+ip+": "+audioPort);
			sock = new Socket(ip, audioPort);
			System.out.println("Audio client connected!");
			outStream = new ObjectOutputStream(sock.getOutputStream());
			inStream  = new ObjectInputStream(sock.getInputStream());
			return true;
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			if(e.getMessage().contains("Connection refused")) {
				System.out.println("Audio connection refused.");
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

	public void addAudioFrameListener(AudioFrameListener l) {
		this.listeners.add(l);
	}
	
	@Override
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
					Object obj = inStream.readObject();
					if(obj!=null) {
						if(obj.getClass().equals(AudioMessage.class)){
							
							for(AudioFrameListener f: listeners) {
								f.onFrameReceived((AudioMessage) obj);
							}
						}else {
							System.out.println(obj.getClass());
						}
					}
					
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch(StreamCorruptedException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
