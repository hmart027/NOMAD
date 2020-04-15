package edu.fiu.cate.nomad.audio.nlp;

import java.io.IOException;

import edu.cmu.sphinx.api.AbstractSpeechRecognizer;
import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.LiveSpeechRecognizer;
import edu.cmu.sphinx.frontend.util.StreamDataSource;
import edu.fiu.cate.nomad.audio.NetworkSpeechStream;

public class NetworkLiveSpeechRecognizer extends AbstractSpeechRecognizer {
	
	NetworkSpeechStream input;
	
	//sampleRate:16000, sampleSize:16, signed:true, bigEndian:false
	public NetworkLiveSpeechRecognizer(Configuration configuration, NetworkSpeechStream input) throws IOException {
		super(configuration);
		this.input = input;
		context.getInstance(StreamDataSource.class).setInputStream(input);
	}
	
	 /**
     * Starts recognition process.
     * @see LiveSpeechRecognizer#stopRecognition()
     */
    public void startRecognition() {
        recognizer.allocate();
        input.start();
    }

    /**
     * Stops recognition process.
     * @see LiveSpeechRecognizer#startRecognition(boolean)
     */
    public void stopRecognition() {
        input.stop();
        recognizer.deallocate();
    }

}
