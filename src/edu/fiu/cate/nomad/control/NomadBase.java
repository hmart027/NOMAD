package edu.fiu.cate.nomad.control;

import java.io.OutputStream;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.protocol.Protocol;

import comm.serial.Comm;
import comm.serial.InputStreamListener;
import edu.fiu.cate.nomad.config.Configurable;
import edu.fiu.cate.nomad.config.NomadConfiguration;

public class NomadBase extends Base implements Configurable, InputStreamListener{

	private Comm comPort = new Comm();
	private int baudrate = 57600;
	private Protocol protocol = new Protocol();
	private volatile boolean msgSent = false;
	private int updateFrequency = 100;
	private double updateInterval = 1.0/(double)updateFrequency;
	
	public NomadBase(){
		
		NomadConfiguration.addConfigurableClass(this);
		NomadConfiguration.loadClassConfiguration(this);
		
		comPort.addInputStreamListener(this);
		new EncoderDataRequest().start();
		new BaseUpdater().start();
		
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
		comPort.closeComm();
	}
	
	@Override
	public void onByteReceived(int d) {
//		System.out.print(Integer.toHexString(d)+" ");
		if(protocol.parseChar((char)d)){
//			System.out.println();
			processPayload(protocol.getPayload());
		}
	}
	
	public class BaseUpdater extends Thread{
		
		public void run(){
			while(true){
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
		public void run(){
			while(true){
				try {
					Thread.sleep(500);
					if(comPort.isConnected()){
						comPort.sendByteArray(Protocol.pack(ENCODER_REQ_PACKET));
//						com.sendByteArray(VELOCITY_REQ_PACKET, 10);
//						msgSent = true;
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
