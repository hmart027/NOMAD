package edu.fiu.cate.nomad.audio;
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

public class JavaSoundAPIMain {
	
	public static PlotterWindow plotter;
	
	JavaSoundAPIMain(){
		
		plotter = new PlotterWindow();
		
		byte[] frame = null;
		Complex[] fft = null;
		double cSample = 0, lSample = 0, lY = 0;
		double dx = 0;

		AudioFormat format = new AudioFormat(22000, 16, 1, true, true);
		int frameSize = format.getFrameSize();
		
//		PlotterWindow.pane1.autoscale(false, false);
		
		try {
			TargetDataLine microphone = AudioSystem.getTargetDataLine(format);
			microphone.open();
			dx = 1d/22000.0;
			frame = new byte[microphone.getBufferSize()/10];
			fft = new Complex[16384];
			int fftCount = 0;
			System.out.println(fft.length);
			microphone.start();
			
			double fStep = 22000.0/(double)fft.length;
				
			while(microphone.isOpen()){
				if(microphone.available() >= frame.length){
					int r = microphone.read(frame, 0, frame.length);
					if(r>0){
						for(int c = 0; c<r; c+=frameSize){
							int data = ((int)frame[c])<<8 | (frame[c+1] & 0x0FF);
							fft[fftCount++] = new Complex(data, 0);
							if(fftCount == fft.length){
								fftCount = 0;
								fft = FFT.fft(fft);
								PlotterWindow.pane2.clearLines();
								for(int x = 0; x<fft.length/2; x++){
									PlotterWindow.pane2.drawLine(x*fStep, 0, x*fStep, fft[x].mod()/(double)fft.length);
								}
//								PlotterWindow.pane2.autoscale(true, false);
							}
							PlotterWindow.pane1.drawLine(lSample, lY, cSample, data);
							double maxX = PlotterWindow.pane1.getMaxX();
							double minX = PlotterWindow.pane1.getMinX();
							double dX = maxX-minX;
							if(maxX<=cSample){
								PlotterWindow.pane1.setMaxX(maxX+dX*0.1);
								PlotterWindow.pane1.setMinX(minX+dX*0.1);
							}
							lSample = cSample;
							cSample += dx;
							lY = data;
						}
					}
				}
			}
			
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}
	}
	
	public static void listInputDevices(){
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
	
	public static void listCameraInputDevices(){
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
	
	public static DataLine getCameraInputDevices(){
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

	public static void getMixerInfo(){
		for(Mixer.Info i: AudioSystem.getMixerInfo()){
			System.out.println(i.getName());
			System.out.println(i.getDescription());
//			Mixer m = AudioSystem.getMixer(i);
			System.out.println();		
		}
	}
	
	public static void main(String[] args) {
		
		listCameraInputDevices();
		
		plotter = new PlotterWindow();
		PlotterWindow.pane1.setMaxY(10000);
		PlotterWindow.pane1.setMinY(-10000);
		PlotterWindow.pane1.setdeltaY(1000);
		PlotterWindow.pane1.setMaxX(5);
		PlotterWindow.pane1.setMinX(0);
		PlotterWindow.pane1.setdeltaX((5-0)*0.1);

		PlotterWindow.pane2.setMaxY(10000);
		PlotterWindow.pane2.setMinY(-10000);
		PlotterWindow.pane2.setdeltaY(1000);
		PlotterWindow.pane2.setMaxX(5);
		PlotterWindow.pane2.setMinX(0);
		PlotterWindow.pane2.setdeltaX((5-0)*0.1);
		
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
		
//		new JavaSoundAPIMain();
		
//		double f = 400;
//		double f1 = 200000;
//
//		Complex[] fft = new Complex[4096];
//
//		int t = 0;
//		for (int i = 0; i < fft.length; i++) {
//			fft[i] = new Complex(Math.sin(2 * Math.PI * f * t / 10000.0), 0);
//			t++;
//		}
//
//		fft = FFT.fft(fft);
//		PlotterWindow.pane2.clearLines();
//		for (int i = 0; i < fft.length / 2; i++) {
//			double x = i / (double) fft.length * 10000;
//			PlotterWindow.pane2.drawLine(x, 0, x, fft[i].mod());
//			if (fft[i].mod() > 100)
//				System.out.println(i + ": " + fft[i].mod());
//		}
//		PlotterWindow.pane2.repaint();
	}
	
	public static class Mic1Listener extends Thread implements LineListener{
		
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
						PlotterWindow.pane1.drawLine(lSample, lch1+6000, cSample, ch1+6000);
						PlotterWindow.pane1.drawLine(lSample, lch2+2000, cSample, ch2+2000, java.awt.Color.RED);
						PlotterWindow.pane1.drawLine(lSample, lch3-2000, cSample, ch3-2000, java.awt.Color.GREEN);
						PlotterWindow.pane1.drawLine(lSample, lch4-6000, cSample, ch4-6000, java.awt.Color.YELLOW);
						double maxX = PlotterWindow.pane1.getMaxX();
						double minX = PlotterWindow.pane1.getMinX();
						if(maxX<=cSample){
							PlotterWindow.pane1.setMaxX(maxX+cSample-lSample);
							PlotterWindow.pane1.setMinX(minX+cSample-lSample);
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
	
	public static class Mic2Listener extends Thread implements LineListener{
		
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
						PlotterWindow.pane2.drawLine(lSample, lch1+6000, cSample, ch1+6000);
						PlotterWindow.pane2.drawLine(lSample, lch2+2000, cSample, ch2+2000, java.awt.Color.RED);
						PlotterWindow.pane2.drawLine(lSample, lch3-2000, cSample, ch3-2000, java.awt.Color.GREEN);
						PlotterWindow.pane2.drawLine(lSample, lch4-6000, cSample, ch4-6000, java.awt.Color.YELLOW);
						double maxX = PlotterWindow.pane2.getMaxX();
						double minX = PlotterWindow.pane2.getMinX();
						if(maxX<=cSample){
							PlotterWindow.pane2.setMaxX(maxX+cSample-lSample);
							PlotterWindow.pane2.setMinX(minX+cSample-lSample);
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
	
}
