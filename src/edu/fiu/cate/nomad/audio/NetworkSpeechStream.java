package edu.fiu.cate.nomad.audio;

import java.io.IOException;
import java.io.InputStream;

import edu.fiu.cate.nomad.rasp.subsystems.messages.AudioMessage;

//to   sampleRate:16000, sampleSize:16, channels:1, signed:true, bigEndian:false
//from sampleRate:16000, sampleSize:16, channels:4, signed:true, bigEndian:true
public class NetworkSpeechStream extends InputStream implements AudioFrameListener{
	
	int eye;
	volatile int readPointer=0;
	volatile int writePointer=0;
	volatile int bufferSize;
	volatile byte[] buffer;
	
	public volatile boolean read, rdy = false;
	
	public NetworkSpeechStream(int eye,int bufferSize){
		this.eye = eye;
		this.bufferSize = bufferSize;
		this.buffer = new byte[bufferSize];
	}
	
	public void start() {
		read =true;
	}
	
	public void stop() {
		read = false;
	}
	

	@Override
	public int read() throws IOException {
		int temp;
		if(readPointer==writePointer) {
			rdy = false;
			try {
				synchronized (this) {
					while(!rdy)
						this.wait();	
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		synchronized (buffer) {
			temp = buffer[readPointer++] & 0x0FF;
			if(readPointer==bufferSize)
				readPointer = 0;
		}
		return temp;
	}

	@Override
	public void onFrameReceived(AudioMessage m) {
		if(m.eye == this.eye && read) {
			boolean notify = (readPointer==writePointer);
			synchronized (buffer) {
				for(int i=0; i<m.ch.length; i+=m.frameSize) {
					int ch = (((int)m.ch[i+0])<<8 | (m.ch[i+1] & 0x0FF) )*2;
//					buffer[writePointer]   = m.ch[i+1];
//					buffer[writePointer+1] = m.ch[i];
					buffer[writePointer]   = (byte) ch;
					buffer[writePointer+1] = (byte) (ch>>8);
					writePointer+=2;
					if(writePointer==bufferSize)
						writePointer = 0;
				}
			}
			if(notify) {
				synchronized (this) {
					rdy = true;
					this.notifyAll();	
				}
			}
		}
	}

}
