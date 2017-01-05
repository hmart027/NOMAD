package edu.fiu.cate.nomad.main;

import java.io.IOException;
import java.io.OutputStream;

import com.protocol.Protocol;

import comm.serial.Comm;
import edu.fiu.cate.nomad.audio.SoundTrials;
import edu.fiu.cate.nomad.control.Base;
import edu.fiu.cate.nomad.gui.NomadGUI;
import edu.fiu.cate.nomad.video.streaming.VideoServer;

@SuppressWarnings("unused")
public class NomadMain {

	private static String NOMAD_PORT = "COM1";
	private static int    NOMAD_BAUD = 9600;

	NomadMain(){
//		new NomadGUI();
//		new VideoServer();
		new SoundTrials();
		
//		for(byte b: Protocol.pack(new byte[]{10, 1, 2, 3, 4, 5, 6}))
//			System.out.print(Integer.toHexString(b&0x0FF)+", ");
				
//		if(System.getProperty("os.name").toLowerCase().equals("linux")){
////			NOMAD_PORT = "/dev/ttyUSB0";
//			NOMAD_PORT = "/dev/ttyS0";
//		}
//		for(String p: Comm.getSerialPortList())
//			System.out.println(p);
//		Comm com = Comm.getCommInstance(NOMAD_PORT, NOMAD_BAUD);
//		if(com==null){
//			System.out.println("Unable to get Serial Port");
//			System.exit(1);
//		}
//		OutputStream out = com.getOutputStream();
//		Base base = Base.getInstance();
//		try {
//			while(true){
//				for(int i = 0; i<=100; i++){
//					Thread.sleep(500);
//					System.out.println("Sending msg: "+i);
//					out.write(Protocol.pack(base.setBaseMotorsPWM(i, 10, 0)));
//				}
//				for(int i = 100; i>=0; i--){
//					Thread.sleep(500);
//					System.out.println("Sending msg: "+i);
//					out.write(Protocol.pack(base.setBaseMotorsPWM(i, 10, 0)));
//				}
//				for(int i = 0; i>=-100; i--){
//					Thread.sleep(500);
//					System.out.println("Sending msg: "+i);
//					out.write(Protocol.pack(base.setBaseMotorsPWM(i, 10, 0)));
//				}
//				for(int i = -100; i<=0; i++){
//					Thread.sleep(500);
//					System.out.println("Sending msg: "+i);
//					out.write(Protocol.pack(base.setBaseMotorsPWM(i, 10, 0)));
//				}
//			}
//			
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}
	
	public static void main(String[] args){
		new NomadMain();
	}
}
