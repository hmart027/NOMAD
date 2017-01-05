package edu.fiu.cate.nomad.audio.nlp;

import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.LiveSpeechRecognizer;
import edu.cmu.sphinx.api.SpeechResult;
import edu.cmu.sphinx.decoder.adaptation.Stats;
import edu.cmu.sphinx.decoder.adaptation.Transform;

public class TranscriberDemo {       
                                     
    public static void main(String[] args) throws Exception {
                                     
        Configuration configuration = new Configuration();

        configuration
                .setAcousticModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us");
        configuration
                .setDictionaryPath("resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict");
        configuration
                .setLanguageModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us.lm.bin");
        
        LiveSpeechRecognizer recognizer = new LiveSpeechRecognizer(configuration);
        
        // Start recognition process pruning previously cached data.
        Stats stats = recognizer.createStats(1);
        System.out.println("Training started");
        recognizer.startRecognition(true);
        SpeechResult result = recognizer.getResult();
        while ((result = recognizer.getResult()) != null) {
        	stats.collect(result);
            System.out.format("Hypothesis: %s\n", result.getHypothesis());
            if(result.getHypothesis().trim().equals("training over")) break;
        }
        // Pause recognition process. It can be resumed then with startRecognition(false).
        System.out.println("Trainning stoped");
        recognizer.stopRecognition();
        
        Transform transform = stats.createTransform();
        recognizer.setTransform(transform);
        System.out.println("Started Listenning");
        recognizer.startRecognition(true);
        result = recognizer.getResult();
        while ((result = recognizer.getResult()) != null) {
            System.out.format("Hypothesis: %s\n", result.getHypothesis());
            if(result.getHypothesis().trim().equals("exit")) break;
        }
        // Pause recognition process. It can be resumed then with startRecognition(false).
        System.out.println("Stoped Listenning");
        recognizer.stopRecognition();
        
    }
}
