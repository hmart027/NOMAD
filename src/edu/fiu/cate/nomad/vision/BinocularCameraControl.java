package edu.fiu.cate.nomad.vision;

import edu.fiu.cate.nomad.rasp.subsystems.messages.SetEyesMessage;
import math2.Math2;

public class BinocularCameraControl{
	
	public static final float CAMERA_OFFSET_THETA 	= 90;
	public static final float CAMERA_OFFSET_PHI 	= 90;
	public static final float CAMERA_OFFSET_X 		= 0.14f;//0.08
	
	public static final int EYE_RIGHT = 1;
	public static final int EYE_LEFT = 2;
	public static final int EYE_NONE = 0;
	
	public static final double MAX_THETA = 60;
	public static final double MIN_THETA = -60;
	public static final double MAX_PHI = 45;
	public static final double MIN_PHI = -45;
	
	
	protected float rXA, rYA, lXA, lYA;
	protected int rXC = 50, rYC = 50, lXC = 50, lYC = 50;    // counts from 0 to 100
	
	protected int updateIntervalMS = 20;
	protected int cameraControlADD = 8;
	
	public BinocularCameraControl() {	}
	

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
		
		rXA = (int) Math.floor(rEyeC[1]);
		rYA = (int) Math.floor(rEyeC[2]);
		lXA = (int) Math.floor(lEyeC[1]);
		lYA = (int) Math.floor(lEyeC[2]);
		
		rXC = (int) (rXA/MAX_THETA*50.0)+50;
		rYC = (int) (rYA/MAX_PHI*50.0)+50;
		lXC = (int) (lXA/MAX_THETA*50.0)+50;
		lYC = (int) (lYA/MAX_PHI*50.0)+50;
	}
	
	public boolean setFocalPoint(int eye, float xAngle, float yAngle, float distance) {
		if(xAngle<MIN_THETA) {
			System.out.println("Requested X-angle is too small. Min is "+MIN_THETA);
			return false;
		}
		if(xAngle>MAX_THETA) {
			System.out.println("Requested X-angle is too large. Max is "+MAX_THETA);
			return false;
		}
		if(yAngle<MIN_PHI) {
			System.out.println("Requested Y-angle is too small. Min is "+MIN_PHI);
			return false;
		}
		if(yAngle>MAX_PHI) {
			System.out.println("Requested Y-angle is too large. Max is "+MAX_PHI);
			return false;
		}
		float[] c = getObjectCoordinates(distance, xAngle, yAngle); // realWorld coordinates
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
	
	public void pointTo(int eye, float xAngle, float yAngle) {
		if(eye==EYE_RIGHT){
			rXA = xAngle;
			rYA = yAngle;
			rXC = (int) (rXA/MAX_THETA*50.0)+50;
			rYC = (int) (rYA/MAX_PHI*50.0)+50;
		}
		if(eye==EYE_LEFT){
			lXA = xAngle;
			lYA = yAngle;
			lXC = (int) (lXA/MAX_THETA*50.0)+50;
			lYC = (int) (lYA/MAX_PHI*50.0)+50;
		}
	}
	
	public void setEyesCounts(int rXC, int rYC, int lXC, int lYC) {
		this.rXC = rXC;
		this.rYC = rYC;
		this.lXC = lXC;
		this.lYC = lYC;
	}
	
	public SetEyesMessage getMessage() {
		return new SetEyesMessage(rXC, rYC, lXC, lYC);
	}
}
