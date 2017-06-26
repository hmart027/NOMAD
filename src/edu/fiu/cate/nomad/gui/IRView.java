package edu.fiu.cate.nomad.gui;

import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class IRView extends JPanel {

	private static final long serialVersionUID = 5972794662246986041L;

	private float maxDistance = 10;
	private int numberOfSensors = 16;
	private float distances[];
	private float movementDirection = 0;
	
	public IRView() {
		this.setPreferredSize(new Dimension(500, 500));
		distances = new float[numberOfSensors];
		for(int i=0; i<numberOfSensors; i++){
			distances[i] = -1;
		}
	}
	
	public void setBounds(int x, int y, int width, int height){
		if(width<height)
			super.setBounds(x, y, width, width);
		else
			super.setBounds(x, y, height, height);
	}
	
	public void paintComponent(Graphics g){
		g.setColor(java.awt.Color.BLACK);
		g.fillRect(0, 0, this.getWidth(), this.getHeight());
		g.setColor(java.awt.Color.BLUE);
		g.fillOval(0, 0, this.getWidth(), this.getHeight());
		int xC = this.getWidth()/2;
		int yC = this.getHeight()/2;
		
		float inc = 360.0f/(float)numberOfSensors;
		float ang;
		int x, y, r;
		r = this.getHeight()/2;

		g.setColor(java.awt.Color.GRAY);
		for(int i=0; i<numberOfSensors; i++){
			ang = (float) Math.toRadians(inc*i);	
			int z = (int) r;
			if(distances[i] >= 0 && distances[i] <= 1){
				z = (int) (r*distances[i]);
			}
			g.fillArc(xC-z, yC-z, 2*(z), 2*(z), (int)Math.toDegrees(ang), (int)Math.round(inc));
		}
		
		g.setColor(java.awt.Color.GREEN);
		for(int i=0; i<numberOfSensors; i++){
			ang = (float) Math.toRadians(inc*i);
			x = (int) (r*Math.sin(ang));
			y = (int) (r*Math.cos(ang));
			g.drawLine(xC, yC, xC+x, yC+y);
		}		

		g.setColor(java.awt.Color.RED);
		g.drawLine(xC, yC-5, xC, yC+5);
		g.drawLine(xC+1, yC-5, xC+1, yC+5);
		g.drawLine(xC-1, yC-5, xC-1, yC+5);
		g.drawLine(xC-5, yC, xC+5, yC);
		g.drawLine(xC-5, yC+1, xC+5, yC+1);
		g.drawLine(xC-5, yC-1, xC+5, yC-1);	
		
		g.drawLine(xC, yC, (int)(xC+r*Math.cos(movementDirection)), (int)(yC+r*Math.sin(movementDirection)));
		
	}

	public void setDistance(int index, float distance){
		distances[index] = distance/maxDistance;
		repaint();
	}
	
	public void setMaxDistance(float maxDistance){
		float corr = this.maxDistance/maxDistance;
		for(int i=0; i<numberOfSensors; i++){
			distances[i] *= corr;
		}
		this.maxDistance = maxDistance;
		repaint();
	}
	
	public void setDirection(double dir){
		movementDirection = (float) Math.toRadians(dir);
	}
	
	public void makeVisible(){
		JFrame f = new JFrame();
		f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		f.setContentPane(this);
		f.setVisible(true);
		f.pack();
	}

}
