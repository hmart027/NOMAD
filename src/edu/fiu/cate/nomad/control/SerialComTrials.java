package edu.fiu.cate.nomad.control;

import com.protocol.Protocol;

import comm.serial.Comm;
import comm.serial.InputStreamListener;
import edu.fiu.cate.nomad.gui.SensorView;

public class SerialComTrials {

	final Protocol protocol = new Protocol();
	SensorView irView = new SensorView();
	int cSensor = 0;
	
	int[] map = new int[]{12, 11, 13, 14, 15, 0, 1, 2, 3, 4, 5, 6, 7, 8, 10, 9};
	
	public SerialComTrials(){
			
		Comm port = new Comm();
		for(String s: Comm.getSerialPortList()){
			System.out.println(s);
		}
//		port.getComm("/dev/ttyS0", 57600);
		port.getComm("/dev/ttyUSB0", 57600);
		port.addInputStreamListener(new InputStreamListener() {
			
			@Override
			public void onByteReceived(int d) {
//				System.out.print((char)d);
//				System.out.println(Integer.toHexString(d));
				if(protocol.parseChar((char)d)){
					byte[] pay = protocol.getPayload();
					if(pay[0]==1){
						setDistance(pay);
					}
				}
				
			}
		});
		
		byte[] msg = Protocol.pack(new byte[]{2,0});
//		byte[] msg = Protocol.pack(new byte[]{1,9});
		irView.setMaxDistance(18000);
		irView.makeVisible();
		
		cSensor = 0;
		while(true){
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			cSensor++;
			if(cSensor==16){
				cSensor = 0;
			}
			msg = Protocol.pack(new byte[]{1,(byte) cSensor});
			port.sendByteArray(msg);
		}
	}
	
	public void setDistance(byte[] pay){
		int d = ((pay[1]&0x00FF)<<8) | (pay[2]&0x00FF);
		irView.setDistance(map[cSensor], d);
	}

}
