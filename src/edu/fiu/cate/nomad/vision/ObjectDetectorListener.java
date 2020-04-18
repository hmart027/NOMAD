package edu.fiu.cate.nomad.vision;

import com.martindynamics.py4j.test.ObjectDetectionResults;

public interface ObjectDetectorListener {

	public void onObjectDetection(ObjectDetectionResults res);
	
}
