package edu.fiu.cate.nomad.vision;

import joystick.DS4;

public class CameraControl {
	DS4 controller;
	
	public CameraControl(){
		
		controller = DS4.getJoystick(100, true);
		
		while(true){
			System.out.println("rx:" + controller.getRX());
			System.out.println("ry:" + controller.getRY());
			System.out.println("ly:" + controller.getLY());
			System.out.println();
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}

}
