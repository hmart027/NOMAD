package edu.fiu.cate.nomad.test;

import java.awt.Dimension;
import java.io.IOException;
import java.util.List;

import javax.swing.JFrame;

import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.SpeechResult;
import edu.cmu.sphinx.result.Lattice;
import edu.fiu.cate.nomad.audio.NOMADAudioClient;
import edu.fiu.cate.nomad.audio.NetworkSpeechStream;
import edu.fiu.cate.nomad.audio.nlp.NetworkLiveSpeechRecognizer;
import plotter.RealTimeGraphPanel;

public class NOMADAudioClientTest {

	public static void main(String[] args) {
		
		Configuration configuration = new Configuration();

		configuration
				.setAcousticModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us");
		configuration
				.setDictionaryPath("resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict");
		configuration
				.setLanguageModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us.lm.bin");
		
		System.out.println("Configuration Loaded.....");
		
		NetworkSpeechStream speechStream = new NetworkSpeechStream(0, 1024);
		NetworkLiveSpeechRecognizer recognizer = null;
		try {
			recognizer = new NetworkLiveSpeechRecognizer(configuration, speechStream);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		NOMADAudioClient audio = new NOMADAudioClient("192.168.0.119");
		audio.addAudioFrameListener(speechStream);
		audio.start();
		
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
