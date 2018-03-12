package edu.fiu.cate.nomad.main;

import java.io.IOException;
import java.io.OutputStream;

import com.protocol.Protocol;

import comm.serial.Comm;
import edu.fiu.cate.nomad.audio.SoundTrials;
import edu.fiu.cate.nomad.audio.nlp.CoreNlpTrial;
import edu.fiu.cate.nomad.audio.nlp.TranscriberDemo;
import edu.fiu.cate.nomad.config.NomadConfiguration;
import edu.fiu.cate.nomad.control.NomadBase;
import edu.fiu.cate.nomad.control.SerialComTrials;
import edu.fiu.cate.nomad.gui.NomadGUI;
import edu.fiu.cate.nomad.video.streaming.VideoServer;
import edu.fiu.cate.nomad.vision.CameraControl;
import edu.stanford.nlp.pipeline.CoreNLPProtos;

@SuppressWarnings("unused")
public class NomadMain {

	NomadMain(){
		NomadConfiguration.loadConfiguration();
//		new NomadGUI();
//		
		new VideoServer().start();
//		new CameraControl();
		
//		new SoundTrials();
//		new TranscriberDemo();
//		new CoreNlpTrial();
		
//		new SerialComTrials();
		
//		for(byte b : Protocol.pack(new byte[] {0xa, 0, 0, 0, 0, 0, 0})){
//			System.out.print(Integer.toHexString(b & 0x0FF)+" ");
//		}
	}
	
	public static void main(String[] args){
		if(args.length==0){
			System.out.println("Empty argument set");
			new NomadMain();
			return;
		}
		
		for(int i = 0; i< args.length; i++){
			System.out.println("\t"+args[i]);
		}
	}
	
}
