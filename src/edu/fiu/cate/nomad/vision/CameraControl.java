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

public class CameraControl extends Thread{
	
	protected DS4 controller;
	protected Comm serial;
	protected Protocol protocol;
	
	protected Filter xFilt, yFilt, zFilt;
	
	public final float CAMERA_OFFSET_THETA 	= 90;
	public final float CAMERA_OFFSET_PHI 	= 90;
	public final float CAMERA_OFFSET_X 		= 0.14f;//0.08
	
	public final int EYE_RIGHT = 1;
	public final int EYE_LEFT = 2;
	public final int EYE_NONE = 0;
	
	public final double MAX_THETA = 60;
	public final double MIN_THETA = -60;
	public final double MAX_PHI = 45;
	public final double MIN_PHI = -45;
	
	protected int rH = 0, rV = 0, lH = 0, lV = 0;	
	
	protected boolean useController, controlling = false;
	
	protected volatile boolean running = false;
		
	public CameraControl(boolean useController){
		
		if(useController){
			initController();
		}
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
		
	}
	
	public boolean initController(){

		controller = DS4.getJoystick(100, true);
		if(controller==null)
			return false;
		// 5Hz lowpass
		double[] b = new double[] { 0.02008333102602092, 0.04016666205204184, 0.02008333102602092 };
		double[] a = new double[] { -1.5610153912536877, 0.6413487153577715 };
		xFilt = IIR.loadIIR(b, a);
		yFilt = IIR.loadIIR(b, a);
		zFilt = IIR.loadIIR(b, a);

		controller.setPollEventListener(new PollEventListener() {
			@Override
			public void onPoll(Gamepad gamepad) {
				xFilt.filter(controller.getRX());
				yFilt.filter(controller.getRY());
				zFilt.filter(controller.getLY());
			}
		});
		
		this.useController = true;
		
		return true;
	}
	
 	public void run(){
		running = true;
		while(running){
			if(useController){
				double x, y, z;
				double rX, rY, lY;
				if(controlling){
					x = Math.round(xFilt.getLastValue() * 100) / 100d;
					y = Math.round(yFilt.getLastValue() * 100) / 100d;
					z = Math.round(zFilt.getLastValue()*100)/100d;
					rX = x*MAX_THETA; // Horizontal angle
					rY = y*MAX_PHI; // Vertical angle
					lY = (-z+1)*6+.05; //Object distance				
					pointToTarget((float)lY, (float)rX, (float)rY, EYE_RIGHT);
				}
				if(controller.getArrows()==0.75){ //down arrow
					controlling= !controlling;
				}				
			}
			if(serial.isConnected())
				serial.sendByteArray(Protocol.pack(new byte[]{0, (byte) rH, (byte) rV, (byte) lH, (byte) lV}));
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void stopRunning(){
		running = false;
	}
	
	public float[] getObjectCoordinates(float r, float theta, float phi){
		return Math2.spherical2cartesian(r, theta + CAMERA_OFFSET_THETA, phi + CAMERA_OFFSET_PHI);
	}
	
	/**
	 * Point the cameras to the target specified by the given coordinates. [0,0,0] is in between 
	 * the two cameras at sensor level.
	 * @param x
	 * @param y
	 * @param z
	 */
	public void pointToTarget(float x, float y, float z){
		float[] rEyeC = Math2.cartesian2spherical(x+CAMERA_OFFSET_X/2, y, z);
		float[] lEyeC = Math2.cartesian2spherical(x-CAMERA_OFFSET_X/2, y, z);
		rEyeC[1]-= CAMERA_OFFSET_THETA;
		lEyeC[1]-= CAMERA_OFFSET_THETA;
		rEyeC[2]-= CAMERA_OFFSET_PHI;
		lEyeC[2]-= CAMERA_OFFSET_PHI;
		
		rH = (int) Math.floor(rEyeC[1]);
		rV = (int) Math.floor(rEyeC[2]);
		lH = (int) Math.floor(lEyeC[1]);
		lV = (int) Math.floor(lEyeC[2]);
//		System.out.println("Sent: "+rH+", "+rV+",  "+lH+", "+lV+"\n");
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
		case EYE_NONE:
			break;
		case EYE_RIGHT:
			c[0] -= CAMERA_OFFSET_X/2;
			break;
		case EYE_LEFT:
			c[0] += CAMERA_OFFSET_X/2;
			break;
		default:
			return false;
		}
		pointToTarget(c[0], c[1], c[2]);
		return true;
	}
}
