package edu.fiu.cate.nomad.vision;

import com.protocol.Protocol;

import comm.serial.Comm;
import comm.serial.InputStreamListener;
import joystick.DS4;
import math2.Math2;

public class CameraControl {
	DS4 controller;
	Comm serial;
	Protocol protocol;
	
	public final float CAMERA_OFFSET_THETA 	= 90;
	public final float CAMERA_OFFSET_PHI 	= 90;
	public final float CAMERA_OFFSET_X 		= 0.08f;
	
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
		
		double rX, rY, lY;
		int rH, rV, lH, lV;
		float[] rCoordiantes, lCoordinates;
		while(true){
			rX = controller.getRX()*90;
			rY = controller.getRY()*90;
			lY = (-controller.getLY()+1)*2.5+.02;
						
			rCoordiantes = Math2.spherical2cartesian((float)lY, (float)rX + CAMERA_OFFSET_THETA, (float)rY + CAMERA_OFFSET_PHI);
			lCoordinates = Math2.cartesian2spherical(rCoordiantes[0]-CAMERA_OFFSET_X, rCoordiantes[1], rCoordiantes[2]);

//			System.out.println("rX:" + rCoordiantes[0]);
//			System.out.println("rY:" + rCoordiantes[1]);
//			System.out.println("rZ:" + rCoordiantes[2]);
//			System.out.println();
//			System.out.println("r-X :" + lY);
//			System.out.println("r-Y :" + lCoordinates[0]);
//			System.out.println("th-X:" + rX);
//			System.out.println("th-Y:" + (lCoordinates[1] - CAMERA_OFFSET_THETA));
//			System.out.println("ph-X:" + rY);
//			System.out.println("ph-Y:" + (lCoordinates[2] - CAMERA_OFFSET_PHI));
//			System.out.println();
			
			rH = (int) (rX);
			rV = (int) (rY);
			lH = (int) (lCoordinates[1] - CAMERA_OFFSET_THETA);
			lV = (int) (lCoordinates[2] - CAMERA_OFFSET_PHI);
						
			serial.sendByteArray(Protocol.pack(new byte[]{0, (byte) rH, (byte) rV, (byte) lH, (byte) lV}));
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}

}
