package edu.fiu.cate.nomad.vision;

import com.martindynamics.video.stream.codec.VideoFrame;

public class FrameEvent {
	public final int chanel;
	public final VideoFrame frame;
	
	public FrameEvent(int c, VideoFrame f) {
		this.chanel = c;
		this.frame = f;
	}
}
