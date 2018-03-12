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
		System.setProperty("gnu.io.rxtx.SerialPorts", "/dev/ttyACM0");
		serial.getComm("/dev/ttyACM0", 57600);
		
		double x, y, z;
		double rX, rY, lY;
		int rH, rV, lH, lV;
		float[] rCoordiantes, lCoordinates;
		
		double c = 0; 
		int dir = 1;
		int t = 0;
		boolean automated = false;
		while(true){
			if(!automated){
				x = Math.round(controller.getRX() * 10) / 10d;
				y = Math.round(controller.getRY() * 10) / 10d;
				z = Math.round(controller.getLY()*100)/100d;
			}else{
				if (dir == 1) {
					c += 0.005;
				} else {
					c -= 0.005;
				}
				if (c >= 1) {
					dir = -1;
				}
				if (c <= -1) {
					dir = 1;
				}
				x = c;
				y = 0;
				z = 0;
			}
			if(controller.getArrows()==0.75){ //down arrow
				automated = !automated;
			}
						
			System.out.println("C: "+x+","+y+","+z);
			
			rX = x*60f; // Horizontal angle
			rY = y*45f; // Vertical angle
			lY = (-z+1)*2.5+.02; //Object distance
			System.out.println("A: "+rX+","+rY+","+lY);
						
			rCoordiantes = Math2.spherical2cartesian((float)lY, (float)rX + CAMERA_OFFSET_THETA, (float)rY + CAMERA_OFFSET_PHI); // realWorld coordinates
			lCoordinates = Math2.cartesian2spherical(rCoordiantes[0]-CAMERA_OFFSET_X, rCoordiantes[1], rCoordiantes[2]); // Left Eye angles

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
			
			rH = (int) Math.floor(rX);
			rV = (int) Math.floor(rY);
			lH = (int) Math.floor(lCoordinates[1] - CAMERA_OFFSET_THETA);
			lV = (int) Math.floor(lCoordinates[2] - CAMERA_OFFSET_PHI);
						
			serial.sendByteArray(Protocol.pack(new byte[]{0, (byte) rH, (byte) rV, (byte) lH, (byte) lV}));
			System.out.println("Sent: "+rH+", "+rV+",  "+lH+", "+lV+"\n");
			try {
				Thread.sleep(100);
				t++;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}

}
