package edu.fiu.cate.nomad.vision;

import com.protocol.Protocol;

import comm.serial.Comm;
import comm.serial.InputStreamListener;
import joystick.DS4;

public class CameraControl {
	DS4 controller;
	Comm serial;
	Protocol protocol;
	
	public CameraControl(){
		
		controller = DS4.getJoystick(100, true);
		serial = new Comm();
		protocol = new Protocol();
		
		serial.addInputStreamListener(new InputStreamListener() {
			
			@Override
			public void onByteReceived(int d) {
				System.out.print((char)(d));
				
			}
		});
		for(String s:Comm.getSerialPortList()){
			System.out.println(s);
		}
		serial.getComm("/dev/ttyUSB0", 9600);
		
		while(true){
//			System.out.println("rx:" + controller.getRX());
//			System.out.println("ry:" + controller.getRY());
//			System.out.println("ly:" + controller.getLY());
//			System.out.println();
			int rX = (int) (controller.getRX()*90);
			int rY = (int) (controller.getRY()*90);
			int lX = (int) (controller.getRX()*90);
			int lY = (int) (controller.getRY()*90);
			System.out.println("cy:" + rY);
			serial.sendByteArray(Protocol.pack(new byte[]{0, (byte) rX, (byte) rY, (byte) lX, (byte) lY}));
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}

}
