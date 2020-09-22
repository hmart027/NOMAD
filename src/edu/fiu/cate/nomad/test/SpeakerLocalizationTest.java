package edu.fiu.cate.nomad.test;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import edu.fiu.cate.nomad.audio.AudioFrameListener;
import edu.fiu.cate.nomad.audio.ICANormalizer;
import edu.fiu.cate.nomad.audio.NOMADAudioClient;
import edu.fiu.cate.nomad.rasp.subsystems.messages.AudioMessage;
import pilsner.fastica.BelowEVFilter;
import pilsner.fastica.FastICA;
import pilsner.fastica.FastICAConfig;
import pilsner.fastica.FastICAException;
import pilsner.fastica.TanhCFunction;
import pilsner.fastica.math.Matrix;
import plotter.SimplePlotter;
import pilsner.fastica.FastICAConfig.Approach;

public class SpeakerLocalizationTest {

	SourceDataLine speaker;
	volatile boolean dataRdy;
	volatile boolean icaRdy;
	
	volatile ICAData ica = new ICAData(4,16000);
	
	volatile int useComp = 0;
	volatile java.util.TreeMap<Double, Integer> orderedComponents = new java.util.TreeMap<>();
	
	public SpeakerLocalizationTest() {

		try {
			speaker = AudioSystem.getSourceDataLine(new AudioFormat(16000, 16, 2, true, true));
			speaker.addLineListener(new LineListener() {
				@Override
				public void update(LineEvent event) {
					System.out.println(event.getType());
				}
			});
			speaker.open();
			speaker.flush();
			speaker.start();
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}
		
		NOMADAudioClient audio = new NOMADAudioClient("192.168.1.198");
		audio.addAudioFrameListener(new AudioFrameListener() {	
			byte[] dat = new byte[4];
			@Override
			public void onFrameReceived(AudioMessage m) {
				if(m.eye==0) {
				//	if(!icaRdy) {
						for(int i=0; i<m.ch.length; i+=m.frameSize) {
							speaker.write(m.ch, i, 4);
						}
				/**	}else{
						double[][] y = new double[m.channelCount][m.t.length];
						int offset = 0,cOff;
						for(int i=0; i<m.t.length; i++) {
							for(int c=0; c<m.channelCount; c++) {
								cOff = offset + c*2;
								y[c][i] = (((int)m.ch[cOff+0])<<8 | (m.ch[cOff+1] & 0x0FF) )*2;
							}
							offset += m.frameSize;
						}
						double[][] s = Matrix.mult(ica.separationMatrix, y);
						double[] z = new double [m.t.length];
						for(int i=0; i<s.length; i++) {
							if(i!=useComp) {
								s[i] = z;
							}
						}
						
						y = Matrix.mult(ica.mixingMatrix,s);

						for(int i=0, j=0; i<m.ch.length; i+=m.frameSize, j++) {
							dat[0] = (byte) (((int)y[0][j])>>8);
							dat[1] = (byte) y[0][j];
							dat[2] = (byte) (((int)y[1][j])>>8);
							dat[3] = (byte) y[1][j];
							speaker.write(dat, 0, 4);
						}
					}*/
					if(!dataRdy) {
						double[][] y = new double[m.channelCount][m.t.length];
						double[] x   = new double[m.t.length];
						int offset = 0,cOff;
						for(int i=0; i<m.t.length; i++) {
							x[i] = m.t[i];
							for(int c=0; c<m.channelCount; c++) {
								cOff = offset + c*2;
								y[c][i] = (((int)m.ch[cOff+0])<<8 | (m.ch[cOff+1] & 0x0FF) )*2;
							}
							offset += m.frameSize;
						}
						if(ica.putSamples(x, y)) {
							dataRdy = true;
						}
					}
				}
			}
		});
		audio.start();
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				while(true)
					speaker.drain();
			}
		}).start();
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (!dataRdy) {
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				ica.performICA();
				autoSelectRDetectionComponent();

				double[][] s = Matrix.mult(ica.separationMatrix, ica.y);
				
				for(int i=0; i<4; i++) {
					SimplePlotter audioPlot = new SimplePlotter("Audio"+i);
					audioPlot.addPlot(ica.x, ica.y[i], java.awt.Color.BLUE);
					SimplePlotter icaPlot = new SimplePlotter("AudioICA"+i);
					icaPlot.addPlot(ica.x, s[i], java.awt.Color.BLUE);
				}
				
			}
		}).start();
		
	}
	
	public void autoSelectRDetectionComponent(){
		ICANormalizer icaNorm = ICANormalizer.getInstance(Matrix.mult(ica.separationMatrix, ica.y)); 
 		double[][] mix = ica.mixingMatrix;
		double[][] mix2 = new double[mix.length][mix[0].length];
		double[] weights = new double[mix[0].length];
		double tot = 0;
		for(int y=0; y<mix2.length; y++){
			for(int x=0; x<mix2[0].length; x++){
				mix2[y][x] = mix[y][x]*3d*icaNorm.std[x];
				weights[x] += mix2[y][x]*mix2[y][x];
				tot += weights[x];
			}
		}
		System.out.println("IC Weight: ");
		double c = 1d/(double)weights.length;
		tot *= c;
		orderedComponents = new java.util.TreeMap<>();
		for(int x=0; x<weights.length; x++){
			orderedComponents.put(weights[x]*(1d-c*x), x);
			System.out.print(Math.round(weights[x]*c/tot*1000d)/1000d+", ");
		}
		System.out.println();
		int comp = orderedComponents.descendingMap().firstEntry().getValue();
		useComp = orderedComponents.descendingMap().lastEntry().getValue();
		System.out.println("Using IC-"+(comp+1)+" for R detection");
		
		System.out.println("Ordered Components: ");
		for(Integer i: orderedComponents.descendingMap().values()){
			System.out.print((i+1)+", ");
		}
		System.out.println();
 	}
	
	/**
	 * Progress listener to keep track of the progress of the ICA computation.
	 * @author harold
	 *
	 */
	public class ProgressListener implements pilsner.fastica.ProgressListener{
		ComputationState pState = null;
		int pComp= -1;
		
		public ProgressListener(){
			super();
		}
		
		@Override
		public void progressMade(ComputationState state, int component,
				int iteration, int maxComps) {
			if(pState != state){
				System.out.print("\n"+state.name());
				pState = state;
				pComp= -1;
			}
			if (pComp != component) {
				System.out.print("\n"+component+": ");
				pComp = component;
			} else {
				System.out.print(".");
			}
		}
		
	}
	
	public class ICAData{
		int index = 0;
		double[] x;
		double[][] y;

		double[][] separationMatrix, mixingMatrix;
		
		public ICAData(int ch, int len) {
			this.x = new double[len];
			this.y = new double[ch][len];
		}
		
		public boolean putSample(double x, double[] ch) {
			this.x[index] = x;
			for(int c=0; c<ch.length; c++) {
				this.y[c][index] = ch[c];
			}
			this.index++;
			if(this.index==this.x.length)
				return true;
			else
				return false;
		}
		
		public boolean putSamples(double[] x, double[][] ch) {
			for(int i=0; i<x.length; i++) {
				this.x[index+i] = x[i];
			}
			for(int c=0; c<ch.length; c++) {
				for(int i=0; i<x.length; i++) {
					this.y[c][index+i] = ch[c][i];
				}
			}
			this.index+=x.length;
			if(this.index==this.x.length)
				return true;
			else
				return false;
		}
		
		public void performICA(){
			try {
				int channelCount = y.length;
				double[][] mMatrix = new double[channelCount][channelCount];
				for (int v = 0; v < channelCount; v++) {
					for (int u = 0; u < channelCount; u++) {
						if (u == v)
							mMatrix[v][u] = 1;
						else
							mMatrix[v][u] = 0;
					}
				}
				FastICA ica = new FastICA(y,
						new FastICAConfig(channelCount, Approach.DEFLATION, 1.0, 1.0e-12, 1000, null),
						new TanhCFunction(1.0), new BelowEVFilter(1.0e-12, false), new ProgressListener());
				separationMatrix = ica.getSeparatingMatrix();
				mixingMatrix = ica.getMixingMatrix();
				icaRdy = true;			
			} catch (FastICAException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	public static void main(String[] args) {
		new SpeakerLocalizationTest();
	}


}
