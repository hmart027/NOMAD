package edu.fiu.cate.nomad.control;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import com.protocol.Protocol;

import edu.fiu.cate.nomad.gui.SensorView;
import edu.fiu.cate.nomad.rasp.subsystems.BaseValues;

public class BaseRasp{
	
	BaseValues vals = new BaseValues(new double[3]);
	Socket sock;
	ObjectOutputStream out;
	ObjectInputStream in;
	
	SensorView irView = null;
	SensorView sonarView = null;
	int[] map = new int[]{12, 11, 13, 14, 15, 0, 1, 2, 3, 4, 5, 6, 7, 8, 10, 9};
	
	public BaseRasp(){
		try {
			sock = new Socket("192.168.1.100", 5050);
//			sock = new Socket("localhost", 5050);
			System.out.println("Connected...");
			out = new ObjectOutputStream(sock.getOutputStream());
			in = new ObjectInputStream(sock.getInputStream());
			new Updater().start();
			new SocketListener().start();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		irView = new SensorView();
		irView.setMaxDistance(18000);
		irView.makeVisible();
		irView.setThreshold(0x2A00);

//		sonarView = new SensorView();
//		sonarView.setMaxDistance(18000);
//		sonarView.makeVisible();
//		sonarView.setThreshold(0x2A00);
		
	}
	
	public void setWheelPWM(int wheel){
		vals.pwm[0] = wheel;
	}
	
	public void setTurnPWM(int turn){
		vals.pwm[1] = turn;
	}
	
	public void setTurretPWM(int turret){
		vals.pwm[2] = turret;
	} 
	
	public class Updater extends Thread{
				
		public void run(){
			try {
				while(true){
					out.reset();
					out.writeObject(vals);
					out.flush();
					try {
						Thread.sleep(60);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public class SocketListener extends Thread{
		boolean running = true;	
		
		public void run(){
			System.out.println("Listenning..");
			BaseValues b;
			while(running && in!=null){
				try {
					if((b=(BaseValues)in.readObject()) !=null){
						vals.ir = b.ir;
						vals.us = b.us;
						irView.setThreshold(b.obsTh);
						for(int i=0; i<16; i++){
							irView.setDistance(map[i], b.ir[i]);
							if(sonarView!=null)
								sonarView.setDistance(i, b.us[i]);
						}
					}
				} catch (IOException | ClassNotFoundException e) {
					e.printStackTrace();
					running = false;
				}
			}
		}
		
	}
}
