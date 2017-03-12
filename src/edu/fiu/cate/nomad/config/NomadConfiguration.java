package edu.fiu.cate.nomad.config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

public class NomadConfiguration {
	
	private static boolean configLoaded = false;
	private static Document configDoc;
	private static ArrayList<Configurable> configurableClasses;
	
	private NomadConfiguration(){}
	
	public static boolean loadConfiguration(){
		return loadConfiguration(null);
	}
	
	public static boolean loadConfiguration(String file){
		configLoaded=false;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			if(file==null || file.length()==0)
				configDoc = builder.parse(new File(NomadConfiguration.class.getResource("/edu/fiu/cate/nomad/config/DefaultConfig.xml").getFile()));
			else
				configDoc = builder.parse(new File(file));
			configLoaded=true;
			reloadConfigurations();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return configLoaded;
	}
	
	public static void addConfigurableClass(Configurable c){
		if(c==null)
			return;
		if(configurableClasses==null)
			configurableClasses = new ArrayList<>();
		configurableClasses.add(c);
	}

	public static boolean loadClassConfiguration(Configurable c){
		if(!configLoaded) return false;
		NodeList configurableClasses = configDoc.getElementsByTagName("configurableClass");
		for(int i=0; i<configurableClasses.getLength(); i++){
			Node n = configurableClasses.item(i);
			if(n.getAttributes().getNamedItem("class").getNodeValue().equals(c.getClass().getName())){
				c.loadConfiguration(n);
				return true;
			}
		}
		return false;
	}
	
	private static void reloadConfigurations(){
		if(configurableClasses==null) 
			return;
		for(Configurable c: configurableClasses){
			loadClassConfiguration(c);
		}
	}
	
}
