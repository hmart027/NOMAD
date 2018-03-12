package edu.fiu.cate.nomad.vision;

import com.protocol.Protocol;

import comm.serial.Comm;
import comm.serial.InputStreamListener;
import edu.fiu.cate.tools.filter.Filter;
import edu.fiu.cate.tools.filter.iir.IIR;
import joystick.DS4;
import joystick.Gamepad;
import joystick.PollEventListener;
import math2.Math2;

public class CameraControl {
	DS4 controller;
	Comm serial;
	Protocol protocol;
	
	Filter xFilt, yFilt, zFilt;
	
	public final float CAMERA_OFFSET_THETA 	= 90;
	public final float CAMERA_OFFSET_PHI 	= 90;
	public final float CAMERA_OFFSET_X 		= 0.14f;//0.08
	
	public final int RIGHT_EYE = 0;
	public final int LEFT_EYE = 1;
	
	public final double MAX_THETA = 60;
	public final double MIN_THETA = -60;
	public final double MAX_PHI = 45;
	public final double MIN_PHI = -45;
	
	
	public CameraControl(){
		
		//5Hz lowpass
		double[] b = new double[]{ 0.02008333102602092, 0.04016666205204184, 0.02008333102602092};
		double[] a = new double[]{-1.5610153912536877, 0.6413487153577715};
		xFilt = IIR.loadIIR(b, a);
		yFilt = IIR.loadIIR(b, a);
		zFilt = IIR.loadIIR(b, a);
		
		controller = DS4.getJoystick(100, true);
		serial = new Comm();
		protocol = new Protocol();
		
		controller.setPollEventListener(new PollEventListener() {
			
			@Override
			public void onPoll(Gamepad gamepad) {
				xFilt.filter(controller.getRX());
				yFilt.filter(controller.getRY());
				zFilt.filter(controller.getLY());
			}
		});
		
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
				x = Math.round(xFilt.getLastValue() * 100) / 100d;
				y = Math.round(yFilt.getLastValue() * 100) / 100d;
				z = Math.round(zFilt.getLastValue()*100)/100d;
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
						
//			System.out.println("C: "+x+","+y+","+z);
			
			rX = x*60f; // Horizontal angle
			rY = y*45f; // Vertical angle
			lY = (-z+1)*2.5+.05; //Object distance
//			System.out.println("A: "+rX+","+rY+","+lY);					
			pointToTarget((float)lY, (float)rX, (float)rY, LEFT_EYE);
			try {
				Thread.sleep(10);
				t++;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	public float[] getObjectCoordinates(float r, float theta, float phi){
		return Math2.spherical2cartesian(r, theta + CAMERA_OFFSET_THETA, phi + CAMERA_OFFSET_PHI);
	}
	
	public void pointToTarget(float x, float y, float z){
		float[] rEyeC = Math2.cartesian2spherical(x+CAMERA_OFFSET_X/2, y, z);
		float[] lEyeC = Math2.cartesian2spherical(x-CAMERA_OFFSET_X/2, y, z);
		rEyeC[1]-= CAMERA_OFFSET_THETA;
		lEyeC[1]-= CAMERA_OFFSET_THETA;
		rEyeC[2]-= CAMERA_OFFSET_PHI;
		lEyeC[2]-= CAMERA_OFFSET_PHI;
		
		int rH = (int) Math.floor(rEyeC[1]);
		int rV = (int) Math.floor(rEyeC[2]);
		int lH = (int) Math.floor(lEyeC[1]);
		int lV = (int) Math.floor(lEyeC[2]);
		serial.sendByteArray(Protocol.pack(new byte[]{0, (byte) rH, (byte) rV, (byte) lH, (byte) lV}));
		System.out.println("Sent: "+rH+", "+rV+",  "+lH+", "+lV+"\n");
		System.out.println();
	}
	
	public boolean pointToTarget(float r, float theta, float phi, int eye){
		if(theta<MIN_THETA){
			System.out.println("Requested Theta too small. Min is "+MIN_THETA);
			return false;
		}
		if(theta>MAX_THETA){
			System.out.println("Requested Theta too large. Max is "+MAX_THETA);
			return false;
		}
		if(phi<MIN_PHI){
			System.out.println("Requested Phi too small. Min is "+MIN_PHI);
			return false;
		}
		if(phi>MAX_PHI){
			System.out.println("Requested Phi too large. Max is "+MAX_PHI);
			return false;
		}
		float[] c = getObjectCoordinates(r, theta, phi); // realWorld coordinates
		switch(eye){
		case RIGHT_EYE:
			c[0] -= CAMERA_OFFSET_X/2;
			break;
		case LEFT_EYE:
			c[0] += CAMERA_OFFSET_X/2;
			break;
		default:
			return false;
		}
		pointToTarget(c[0], c[1], c[2]);
		return true;
	}
}
