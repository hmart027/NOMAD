package edu.fiu.cate.nomad.test;

import java.io.IOException;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.SpeechResult;
import edu.fiu.cate.nomad.audio.AudioFrameListener;
import edu.fiu.cate.nomad.audio.NOMADAudioClient;
import edu.fiu.cate.nomad.audio.NetworkSpeechStream;
import edu.fiu.cate.nomad.audio.nlp.NetworkLiveSpeechRecognizer;
import edu.fiu.cate.nomad.rasp.subsystems.messages.AudioMessage;

public class NOMADAudioClientTest {
	
	static SourceDataLine speaker;

	public static void main(String[] args) {
		
		Configuration configuration = new Configuration();
		configuration.setAcousticModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us");
		configuration.setDictionaryPath("resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict");
		configuration.setLanguageModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us.lm.bin");
		
		System.out.println("Configuration Loaded.....");
		
		NetworkSpeechStream speechStream = new NetworkSpeechStream(0, 1024);
		NetworkLiveSpeechRecognizer recognizer = null;
		try {
			recognizer = new NetworkLiveSpeechRecognizer(configuration, speechStream);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			speaker = AudioSystem.getSourceDataLine(new AudioFormat(16000, 16, 1, true, true));
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
		audio.addAudioFrameListener(speechStream);
		audio.addAudioFrameListener(new AudioFrameListener() {
			@Override
			public void onFrameReceived(AudioMessage m) {
				if(m.eye==0) {
					for(int i=0; i<m.ch.length; i+=m.frameSize) {
						speaker.write(m.ch, i, 2);
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
		
		System.out.println("Started Listenning");
		recognizer.startRecognition();
		
		SpeechResult result = recognizer.getResult();
		while ((result = recognizer.getResult()) != null) {
			System.out.format("Hypothesis: %s\n", result.getHypothesis());
			if (result.getHypothesis().trim().equals("exit"))
				break;
		}
		// Pause recognition process. It can be resumed then with
		// startRecognition(false).
		System.out.println("Stoped Listenning");
		recognizer.stopRecognition();
		
	}

}
