package edu.fiu.cate.nomad.control;

import java.io.OutputStream;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.protocol.Protocol;

import comm.serial.Comm;
import comm.serial.InputStreamListener;
import edu.fiu.cate.nomad.config.Configurable;
import edu.fiu.cate.nomad.config.NomadConfiguration;
import edu.fiu.cate.nomad.gui.IRView;

public class NomadBase extends Base implements Configurable, InputStreamListener{

	private Comm comPort = new Comm();
	private int baudrate = 57600;
	private Protocol protocol = new Protocol();
	private volatile boolean msgSent = false;
	private int updateFrequency = 100;
	private double updateInterval = 1.0/(double)updateFrequency;
	
	IRView irView = new IRView();
	int[] map = new int[]{12, 11, 13, 14, 15, 0, 1, 2, 3, 4, 5, 6, 7, 8, 10, 9};
	
	EncoderDataRequest eData;
	BaseUpdater updater;
	
	public NomadBase(){
		
		NomadConfiguration.addConfigurableClass(this);
		NomadConfiguration.loadClassConfiguration(this);
		
		comPort.addInputStreamListener(this);
		eData = new EncoderDataRequest();
		updater = new BaseUpdater();
		
		eData.start();
		updater.start();
		
		irView.setMaxDistance(18000);
		irView.makeVisible();
		
	}

	@Override
	public boolean loadConfiguration(Node config) {
		System.out.println("Loading NomadBase configuration.");
		NodeList variableNodes = config.getChildNodes();
		for(int v=0; v<variableNodes.getLength(); v++){
			Node var = variableNodes.item(v);
			if(!var.getNodeName().equals("variable")) continue;
			switch(var.getAttributes().getNamedItem("name").getNodeValue()){
			case "boudrate":
				this.baudrate = Integer.parseInt(var.getAttributes().getNamedItem("value").getNodeValue());
				break;
			case "updateFrequency":
				this.updateFrequency = Integer.parseInt(var.getAttributes().getNamedItem("value").getNodeValue());
				this.updateInterval = 1.0/(double)updateFrequency;
				break;
			}
		}
		return false;
	}

	@Override
	public boolean saveConfiguration(Node parentNode) {
		// TODO Auto-generated method stub
		return false;
	};
	
	public boolean connectTo(String portName){
		System.out.println("Conecting to: "+portName+": "+baudrate);
		return comPort.getComm(portName, baudrate);
	}
	
	public void disconnect(){
		eData.stopThread();
		updater.stopThread();
		comPort.closeComm();
	}
	
	@Override
	public void onByteReceived(int d) {
//		System.out.print(Integer.toHexString(d)+" ");
//		System.out.print((char)(d));
		if(protocol.parseChar((char)d)){
//			System.out.println();
			processPayload(protocol.getPayload());
		}
	}
	
	public class BaseUpdater extends Thread{

		volatile boolean run = true;
		
		public void stopThread(){
			run = false;
		}
		
		public void run(){
			while(run){
				if(comPort.isConnected()){
					byte[] msg = Protocol.pack(Base.getBaseMotorsPWMMessage());
					comPort.sendByteArray(msg);
					msgSent = true;
				}
				try {
					Thread.sleep((long) (updateInterval*1000));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public class KeepAliveHeartbeat extends Thread{
		OutputStream out;
		KeepAliveHeartbeat( OutputStream o){
			out = o;
		}
		public void run(){
			while(comPort.isConnected()){
				try {
					if(!msgSent)
						comPort.sendByteArray(HB_PACKET);
					else
						msgSent = false;
					Thread.sleep(400);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public class EncoderDataRequest extends Thread{
		
		volatile boolean run = true;
		
		public void stopThread(){
			run = false;
		}
		
		public void run(){
			while(run){
				try {
					Thread.sleep(250);
					if(comPort.isConnected()){
						comPort.sendByteArray(Protocol.pack(ENCODER_REQ_PACKET));
						int max = 135700;
						double w = 360d/(double)max;
						System.out.println("Enc: "+ turningEncoderCount*w +", "+ turretEncoderCount*w);
						double ang = (turningEncoderCount - turretEncoderCount)*w;
						irView.setDirection(ang);

						Thread.sleep(250);
						comPort.sendByteArray(Protocol.pack(IR_REQ_PACKET));
//						comPort.sendByteArray(Protocol.pack(new byte[]{21}));
						for(int i=0; i<16; i++){
							irView.setDistance(map[i], irSensors[i]);
						}
												
//						com.sendByteArray(VELOCITY_REQ_PACKET, 10);
//						msgSent = true;
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public class IRDataRequest extends Thread{

		volatile boolean run = true;
		
		public void stopThread(){
			run = false;
		}
		
		public void run(){
			while(run){
				try {
					Thread.sleep(500);
					if(comPort.isConnected()){
						comPort.sendByteArray(Protocol.pack(IR_REQ_PACKET));
//						msgSent = true;
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
