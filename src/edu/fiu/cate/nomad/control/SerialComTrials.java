package edu.fiu.cate.nomad.control;

import com.protocol.Protocol;

import comm.serial.Comm;
import comm.serial.InputStreamListener;

public class SerialComTrials {
	
	public SerialComTrials(){
		Comm port = new Comm();
		for(String s: Comm.getSerialPortList()){
			System.out.println(s);
		}
//		port.getComm("/dev/ttyS0", 57600);
		port.getComm("/dev/ttyUSB0", 57600);
		port.addInputStreamListener(new InputStreamListener() {
			
			@Override
			public void onByteReceived(int d) {
				System.out.print((char)d);
				
			}
		});
		
		byte[] msg = Protocol.pack(new byte[]{2,0});
		
		while(true){
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
//			port.sendByteArray(msg);
		}
	}

}
