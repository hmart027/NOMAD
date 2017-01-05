package edu.fiu.cate.nomad.audio.nlp;

import java.io.IOException;

import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.LiveSpeechRecognizer;
import edu.cmu.sphinx.api.SpeechResult;
import edu.cmu.sphinx.decoder.adaptation.Stats;
import edu.cmu.sphinx.decoder.adaptation.Transform;
import edu.fiu.cate.nomad.audio.PSEyeAudio;

public class TranscriberDemo {     
	
	public TranscriberDemo(){

		Configuration configuration = new Configuration();

		configuration
				.setAcousticModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us");
		configuration
				.setDictionaryPath("resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict");
		configuration
				.setLanguageModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us.lm.bin");
		
		System.out.println("Configuration Loaded.....");

		try {
			PSEyeAudio[] cams = PSEyeAudio.getAvailablePSEye();
			LiveSpeechRecognizer recognizer = new LiveSpeechRecognizer(configuration, cams[0].getMixer().getMixerInfo());

			System.out.println("Recognizer Created.....");

			// Start recognition process pruning previously cached data.
			Stats stats = recognizer.createStats(1);
			System.out.println("Training started");
			recognizer.startRecognition(true);
			SpeechResult result = recognizer.getResult();
			while ((result = recognizer.getResult()) != null) {
				stats.collect(result);
				System.out.format("Hypothesis: %s\n", result.getHypothesis());
				if (result.getHypothesis().trim().equals("training over"))
					break;
			}
			// Pause recognition process. It can be resumed then with
			// startRecognition(false).
			System.out.println("Trainning stoped");
			recognizer.stopRecognition();

			Transform transform = stats.createTransform();
			recognizer.setTransform(transform);
			System.out.println("Started Listenning");
			recognizer.startRecognition(true);
			result = recognizer.getResult();
			while ((result = recognizer.getResult()) != null) {
				System.out.format("Hypothesis: %s\n", result.getHypothesis());
				if (result.getHypothesis().trim().equals("exit"))
					break;
			}
			// Pause recognition process. It can be resumed then with
			// startRecognition(false).
			System.out.println("Stoped Listenning");
			recognizer.stopRecognition();
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
}
