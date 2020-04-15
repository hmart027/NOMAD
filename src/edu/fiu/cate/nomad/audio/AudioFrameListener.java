package edu.fiu.cate.nomad.audio;

import edu.fiu.cate.nomad.rasp.subsystems.messages.AudioMessage;

public interface AudioFrameListener {

	public void onFrameReceived(AudioMessage m);
	
}
