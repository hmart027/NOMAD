package edu.fiu.cate.nomad.audio;

import java.awt.Dimension;
import java.awt.Toolkit;
import javax.swing.JPanel;

import main.Window;
import plotter.GraphPanel;

public class PlotterWindow{		
	
	private static Window window; 			// The application window
	public static GraphPanel pane1 = null; 		// Pane containing filled rectangles
	public static GraphPanel pane2 = null; 		// Pane containing filled rectangles
    
    public PlotterWindow(){

    	this.creatGUI();
    	   	
    	pane1.setMaxX(5);
    	pane1.setMinX(0);
    	pane1.setdeltaX((5-0)*0.1);
    	pane1.setMaxY(100);
    	pane1.setMinY(-100);
    	pane1.setdeltaY(10);
    	
    	pane2.setMaxX(1000);
    	pane2.setMinX(0);
    	pane2.setdeltaX(100);
    	pane2.setMaxY(2);
    	pane2.setMinY(-2);
    	pane2.setdeltaY(0.1);
    	
    }
	
	// Method to create the application GUI
	private void creatGUI() {
		
		window = new Window("Ocilloscope"); 			// Create the app window
		Toolkit theKit = window.getToolkit(); 		// Get the window toolkit
		Dimension wndSize = theKit.getScreenSize(); // Get screen size
		
		int width = wndSize.width;
		int height = wndSize.height;
		
		int sizeFactor = 90;
		
		if(width>height){
			width = height;
			height = height-50;
		}
		if(height>width){
			height = width;
		}
		
		Dimension prefdim = new Dimension(width,height);//Preffered dimensions
		
		// Set the position & size of window
		window.setBounds(0, 0, 	// Position
				width*sizeFactor/100, height*sizeFactor/100); 				// Size
		window.setPreferredSize(prefdim);

		window.setVisible(true);		// Shows window
		window.pack();					// Packs window
		
		//Sends the size to the class
		Dimension dim = window.getContentPane().getSize();
		pane1 = new GraphPanel(dim.width,dim.height/2-2); // Pane containing filled rectangles
		pane2 = new GraphPanel(dim.width,dim.height/2-2); // Pane containing filled rectangles
		
		//Adds the background picture for the first time
		JPanel pane = new JPanel();
		pane.setPreferredSize(dim);
		pane.add(pane1);
		pane.add(pane2);
		window.getContentPane().add(pane); 
		
		window.setResizable(false);		// Prevents resizing
				
//		System.out.println("Width:  "+pane.dim.width);
//		System.out.println("Heigth: "+pane.dim.height);

	}
	
}