package edu.fiu.cate.nomad.video.streaming;

import image.tools.IViewer;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JFrame;

import com.xuggle.mediatool.IMediaReader;
import com.xuggle.mediatool.MediaListenerAdapter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.mediatool.event.IVideoPictureEvent;
import com.xuggle.xuggler.io.XugglerIO;

public class VideoClient {
	IViewer v = null;

	public VideoClient(){
		try {
			Socket s = new Socket("localhost", 5050);
			
			IMediaReader reader = ToolFactory.makeReader(XugglerIO.map(s.getInputStream()));
			reader.setBufferedImageTypeToGenerate(BufferedImage.TYPE_3BYTE_BGR);
			reader.addListener(new ImageSnapListener());

			while (reader.readPacket() == null) ;

			s.close();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		new VideoClient();
	}
	
	private class ImageSnapListener extends MediaListenerAdapter {

		public void onVideoPicture(IVideoPictureEvent event) {
			
//			System.out.println(event.getSource());
			
			BufferedImage img = event.getImage();
			if(v==null){
				v=new IViewer("Client", img);
				v.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			}else{
				v.setImage(img);
			}

		}

	}

}
