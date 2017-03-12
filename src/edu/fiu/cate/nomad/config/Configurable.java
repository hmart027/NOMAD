package edu.fiu.cate.nomad.config;

import org.w3c.dom.Node;

public interface Configurable {
	
	public boolean loadConfiguration(Node config);
	public boolean saveConfiguration(Node parentNode);

}
