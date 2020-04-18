package edu.fiu.cate.nomad.vision;

import com.martindynamics.video.stream.VideoPacket;

public interface VideoPacketListener {
	
	public void onPacketReceived(VideoPacket pck);
	
}
