package edu.fiu.cate.nomad.config;

import java.io.File;
import java.io.IOException;
//import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

public class NomadConfiguration {
	
	private static boolean configLoaded = false;
	private static Document configDoc;
	private static ArrayList<ConfigurationChangeListener> configListeners;
	
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
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return configLoaded;
	}

	public void addConfigurationChangeListener(ConfigurationChangeListener c){
		if(c==null)
			return;
		if(configListeners==null)
			configListeners = new ArrayList<>();
		configListeners.add(c);
	}

	public void refresh(){
		if(configListeners==null)
			return;
//		for(ConfigurationChangeListener c: configListeners)
//			c.onConfigurationChange(this);
	}
		
/*	public boolean setConfigurationVariables(Object c){
		if(configDoc==null)
			return false;
		NodeList elements = configDoc.getElementsByTagName(c.getClass().getSimpleName());
		NodeList functions = elements.item(0).getChildNodes();
		for(int i=0; i<functions.getLength();i++){
			Node function = functions.item(i);
			//Skip nodes that are not named "function"
			if(!function.getNodeName().equals("function")) continue;
			String functionName = function.getAttributes().getNamedItem("name").getNodeValue();
			
			//Get the parameter list and values 
			NodeList paramList = function.getChildNodes();
			ArrayList<Class<?>> paramsClasses = new ArrayList<>();
			ArrayList<Object> paramsObjects = new ArrayList<>();
			for(int p=0; p<paramList.getLength(); p++){
				Node param = paramList.item(p);
				if(!param.getNodeName().equals("param")) continue;
				String className = param.getAttributes().getNamedItem("type").getNodeValue();
				String value = param.getAttributes().getNamedItem("value").getNodeValue();
				try {
					paramsClasses.add(Class.forName(className));
					switch(Class.forName(className).getSimpleName()){
					case "String":
						paramsObjects.add(value);
						break;
					case "Integer":
						paramsObjects.add(Integer.parseInt(value));
						break;
					case "Float":
						paramsObjects.add(Float.parseFloat(value));
						break;
					case "Double":
						paramsObjects.add(Double.parseDouble(value));
						break;
					}
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
			
			try {
				java.lang.reflect.Method method = c.getClass().getMethod(functionName, paramsClasses.toArray(new Class<?>[paramsClasses.size()]));
				if(method!=null)
					method.invoke(c, paramsObjects.toArray());
			} catch (SecurityException e) {
				
			} catch (NoSuchMethodException e) {
				
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
		
		return true;
	}*/
	
}
