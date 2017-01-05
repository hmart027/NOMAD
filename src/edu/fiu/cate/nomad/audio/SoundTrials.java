package edu.fiu.cate.nomad.audio;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.dsp.tools.Complex;
import javax.dsp.tools.FFT;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;
import javax.swing.JPanel;

import main.Window;
import plotter.GraphPanel;

public class SoundTrials {
	
	public PlotterWindow plotter;
	
	public SoundTrials(){
		
		listCameraInputDevices();
		
		plotter = new PlotterWindow();
		plotter.pane1.setMaxY(10000);
		plotter.pane1.setMinY(-10000);
		plotter.pane1.setdeltaY(1000);
		plotter.pane1.setMaxX(5);
		plotter.pane1.setMinX(0);
		plotter.pane1.setdeltaX((5-0)*0.1);

		plotter.pane2.setMaxY(10000);
		plotter.pane2.setMinY(-10000);
		plotter.pane2.setdeltaY(1000);
		plotter.pane2.setMaxX(5);
		plotter.pane2.setMinX(0);
		plotter.pane2.setdeltaX((5-0)*0.1);
		
		PSEyeAudio[] cams = PSEyeAudio.getAvailablePSEye();
		try {
			DataLine cam1 = cams[0].getTargetDataLine();
			cam1.addLineListener(new Mic1Listener());
			cam1.open();
			
			DataLine cam2 = cams[1].getTargetDataLine();
			cam2.addLineListener(new Mic2Listener());
			cam2.open();
			
		} catch (LineUnavailableException e1) {
			e1.printStackTrace();
		}
				
//		byte[] frame = null;
//		Complex[] fft = null;
//		double cSample = 0, lSample = 0, lY = 0;
//		double dx = 0;
//
//		AudioFormat format = new AudioFormat(22000, 16, 1, true, true);
//		int frameSize = format.getFrameSize();
//		
////		PlotterWindow.pane1.autoscale(false, false);
//		
//		try {
//			TargetDataLine microphone = AudioSystem.getTargetDataLine(format);
//			microphone.open();
//			dx = 1d/22000.0;
//			frame = new byte[microphone.getBufferSize()/10];
//			fft = new Complex[16384];
//			int fftCount = 0;
//			System.out.println(fft.length);
//			microphone.start();
//			
//			double fStep = 22000.0/(double)fft.length;
//				
//			while(microphone.isOpen()){
//				if(microphone.available() >= frame.length){
//					int r = microphone.read(frame, 0, frame.length);
//					if(r>0){
//						for(int c = 0; c<r; c+=frameSize){
//							int data = ((int)frame[c])<<8 | (frame[c+1] & 0x0FF);
//							fft[fftCount++] = new Complex(data, 0);
//							if(fftCount == fft.length){
//								fftCount = 0;
//								fft = FFT.fft(fft);
//								PlotterWindow.pane2.clearLines();
//								for(int x = 0; x<fft.length/2; x++){
//									PlotterWindow.pane2.drawLine(x*fStep, 0, x*fStep, fft[x].mod()/(double)fft.length);
//								}
////								PlotterWindow.pane2.autoscale(true, false);
//							}
//							PlotterWindow.pane1.drawLine(lSample, lY, cSample, data);
//							double maxX = PlotterWindow.pane1.getMaxX();
//							double minX = PlotterWindow.pane1.getMinX();
//							double dX = maxX-minX;
//							if(maxX<=cSample){
//								PlotterWindow.pane1.setMaxX(maxX+dX*0.1);
//								PlotterWindow.pane1.setMinX(minX+dX*0.1);
//							}
//							lSample = cSample;
//							cSample += dx;
//							lY = data;
//						}
//					}
//				}
//			}
//			
//		} catch (LineUnavailableException e) {
//			e.printStackTrace();
//		}
	}
	
	public void listInputDevices(){
		Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
		System.out.println("Mixers: ");
		for (Mixer.Info info: mixerInfos){
			Mixer m = AudioSystem.getMixer(info);
			if(m.getTargetLineInfo().length<=0) continue;
			System.out.println(info);
			System.out.println(info.getDescription());

			Line.Info[] lineInfos;
//			System.out.println("\tSource Lines: ");
//			lineInfos = m.getSourceLineInfo();
//			for (Line.Info lineInfo:lineInfos){
//				System.out.println ("\t\t"+info.getName()+"---"+lineInfo);
////				Line line = m.getLine(lineInfo);
////				System.out.println("\t-----"+line);
//			}
			
			System.out.println("\tTarget Lines: ");
			lineInfos = m.getTargetLineInfo();
			for (Line.Info lineInfo:lineInfos){
				System.out.println ("\t\t"+m+"---"+lineInfo);
//				Line line = m.getLine(lineInfo);
//				System.out.println("\t-----"+line);
			}
			System.out.println();
		 }
	}
	
	public void listCameraInputDevices(){
		Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
		System.out.println("Mixers: ");
		for (Mixer.Info info: mixerInfos){
			// Get only mixes that contain the string "camera" in their names
			if(!info.getName().toLowerCase().contains("camera")) continue;
			
			Mixer m = AudioSystem.getMixer(info);
			//If the mixer contain no targetDataLines (no input lines) skip it
			if(m.getTargetLineInfo().length<=0) continue;
			
			System.out.println(info);
			System.out.println("\t"+info.getDescription());

			System.out.println("\tTarget Lines: ");
			Line.Info[] lineInfos;			
			lineInfos = m.getTargetLineInfo();
			for (Line.Info lineInfo:lineInfos){
				System.out.println ("\t\t"+m+"---"+lineInfo);
				
				for(AudioFormat format: ((DataLine.Info)lineInfo).getFormats()){
					System.out.println("\t\t\t"+format);
				}
						
				try {
					DataLine line = (DataLine) m.getLine(lineInfo);
					System.out.println("\t"+line.getFormat());
				} catch (LineUnavailableException e) {
					e.printStackTrace();
				}
			}
			System.out.println();
		 }
	}
	
	public DataLine getCameraInputDevices(){
		Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
		for (Mixer.Info info: mixerInfos){
			// Get only mixes that contain the string "camera" in their names
			if(!info.getName().toLowerCase().contains("camera")) continue;
			
			Mixer m = AudioSystem.getMixer(info);
			//If the mixer contain no targetDataLines (no input lines) skip it
			if(m.getTargetLineInfo().length<=0) continue;
										
			try {
				return (DataLine) m.getLine(new DataLine.Info(DataLine.class,
						new AudioFormat(44100, 16, 4, true, true)));
			} catch (LineUnavailableException e) {
				e.printStackTrace();
			}
		 }
		return null;
	}

	public void getMixerInfo(){
		for(Mixer.Info i: AudioSystem.getMixerInfo()){
			System.out.println(i.getName());
			System.out.println(i.getDescription());
//			Mixer m = AudioSystem.getMixer(i);
			System.out.println();		
		}
	}
		
	public class Mic1Listener extends Thread implements LineListener{
		
		private TargetDataLine microphone;
		
		byte[]	frame;
		double cSample = 0, lSample = 0, lch1 = 0, lch2 = 0, lch3 = 0, lch4 = 0;
		double dx = 1d/44100.0;
		
		@Override
		public void run(){

			int fCount = 0;
			while(microphone.isOpen()){
				if(microphone.available() >= frame.length){
					fCount++;
					((TargetDataLine) microphone).read(frame, 0, frame.length);
					double ch1 = ((int)frame[0])<<8 | (frame[1] & 0x0FF);
					double ch2 = ((int)frame[2])<<8 | (frame[3] & 0x0FF);
					double ch3 = ((int)frame[4])<<8 | (frame[5] & 0x0FF);
					double ch4 = ((int)frame[6])<<8 | (frame[7] & 0x0FF);
					
					if(fCount>=100){
						plotter.pane1.drawLine(lSample, lch1+6000, cSample, ch1+6000);
						plotter.pane1.drawLine(lSample, lch2+2000, cSample, ch2+2000, java.awt.Color.RED);
						plotter.pane1.drawLine(lSample, lch3-2000, cSample, ch3-2000, java.awt.Color.GREEN);
						plotter.pane1.drawLine(lSample, lch4-6000, cSample, ch4-6000, java.awt.Color.YELLOW);
						double maxX = plotter.pane1.getMaxX();
						double minX = plotter.pane1.getMinX();
						if(maxX<=cSample){
							plotter.pane1.setMaxX(maxX+cSample-lSample);
							plotter.pane1.setMinX(minX+cSample-lSample);
						}
						lSample = cSample;
						lch1 = ch1;
						lch2 = ch2;
						lch3 = ch3;
						lch4 = ch4;
						fCount = 0;
					}
					cSample += dx;
				}
			}
		}
		
		@Override
		public void update(LineEvent event) {
			if (event.getType().equals(LineEvent.Type.OPEN)) {
				microphone = (TargetDataLine) event.getSource();
				microphone.start();
				frame = new byte[microphone.getFormat().getFrameSize()];
				this.start();
			}		
			
		}
		
	}
	
	public class Mic2Listener extends Thread implements LineListener{
		
		private TargetDataLine microphone;
		
		byte[]	frame;
		double cSample = 0, lSample = 0, lch1 = 0, lch2 = 0, lch3 = 0, lch4 = 0;
		double dx = 1d/44100.0;
		
		@Override
		public void run(){

			int fCount = 0;
			while(microphone.isOpen()){
				if(microphone.available() >= frame.length){
					fCount++;
					((TargetDataLine) microphone).read(frame, 0, frame.length);
					double ch1 = ((int)frame[0])<<8 | (frame[1] & 0x0FF);
					double ch2 = ((int)frame[2])<<8 | (frame[3] & 0x0FF);
					double ch3 = ((int)frame[4])<<8 | (frame[5] & 0x0FF);
					double ch4 = ((int)frame[6])<<8 | (frame[7] & 0x0FF);
					
					if(fCount>=100){
						plotter.pane2.drawLine(lSample, lch1+6000, cSample, ch1+6000);
						plotter.pane2.drawLine(lSample, lch2+2000, cSample, ch2+2000, java.awt.Color.RED);
						plotter.pane2.drawLine(lSample, lch3-2000, cSample, ch3-2000, java.awt.Color.GREEN);
						plotter.pane2.drawLine(lSample, lch4-6000, cSample, ch4-6000, java.awt.Color.YELLOW);
						double maxX = plotter.pane2.getMaxX();
						double minX = plotter.pane2.getMinX();
						if(maxX<=cSample){
							plotter.pane2.setMaxX(maxX+cSample-lSample);
							plotter.pane2.setMinX(minX+cSample-lSample);
						}
						lSample = cSample;
						lch1 = ch1;
						lch2 = ch2;
						lch3 = ch3;
						lch4 = ch4;
						fCount = 0;
					}
					cSample += dx;
				}
			}
		}
		
		@Override
		public void update(LineEvent event) {
			if (event.getType().equals(LineEvent.Type.OPEN)) {			
				microphone = (TargetDataLine) event.getSource();
				microphone.start();
				frame = new byte[microphone.getFormat().getFrameSize()];
				this.start();
			}
			
		}
		
	}

	public class PlotterWindow{		
		
		private Window window; 			// The application window
		public GraphPanel pane1 = null; 		// Pane containing filled rectangles
		public GraphPanel pane2 = null; 		// Pane containing filled rectangles
	    
	    public PlotterWindow(){

	    	window = new Window("Plotter"); 			// Create the app window
			Toolkit theKit = window.getToolkit(); 		// Get the window toolkit
			Dimension wndSize = theKit.getScreenSize(); // Get screen size
			
			int width = wndSize.width;
			int height = wndSize.height;
			
			int sizeFactor = 90;
			
			if(width>height){
				width = height;
				height = height-50;
			}
			if(height>width){
				height = width;
			}
			
			Dimension prefdim = new Dimension(width,height);//Preffered dimensions
			// Set the position & size of window
			window.setBounds(0, 0, width*sizeFactor/100, height*sizeFactor/100); 	
			window.setPreferredSize(prefdim);
			window.setVisible(true);		// Shows window
			window.pack();					// Packs window
			
			//Sends the size to the class
			Dimension dim = window.getContentPane().getSize();
			pane1 = new GraphPanel(dim.width,dim.height/2-2); // Pane containing filled rectangles
			pane2 = new GraphPanel(dim.width,dim.height/2-2); // Pane containing filled rectangles
			
			//Adds the background picture for the first time
			JPanel pane = new JPanel();
			pane.setPreferredSize(dim);
			pane.add(pane1);
			pane.add(pane2);
			window.getContentPane().add(pane); 
			
			window.setResizable(false);		// Prevents resizing
	    	   	
	    	pane1.setMaxX(5);
	    	pane1.setMinX(0);
	    	pane1.setdeltaX((5-0)*0.1);
	    	pane1.setMaxY(100);
	    	pane1.setMinY(-100);
	    	pane1.setdeltaY(10);
	    	
	    	pane2.setMaxX(1000);
	    	pane2.setMinX(0);
	    	pane2.setdeltaX(100);
	    	pane2.setMaxY(2);
	    	pane2.setMinY(-2);
	    	pane2.setdeltaY(0.1);
	    	
	    }
		
	}
}
