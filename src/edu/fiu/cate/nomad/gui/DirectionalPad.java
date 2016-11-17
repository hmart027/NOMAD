package edu.fiu.cate.nomad.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JPanel;

public class DirectionalPad extends JPanel {

	private static final long serialVersionUID = 6280338996159812714L;
	
	private Dimension dim;
	private boolean up, down, left, right;
	int[] upX, downX, leftX, rightX;
	int[] upY, downY, leftY, rightY;
	
	public DirectionalPad(Dimension dim){
		this.dim = dim;
		this.setPreferredSize(dim);
		createArrows();
	}
	
	public void pressUP(boolean pressed){
		this.up = pressed;
		repaint();
	}
	
	public void pressDown(boolean pressed){
		this.down = pressed;
		repaint();
	}
	
	public void pressLeft(boolean pressed){
		this.left = pressed;
		repaint();
	}
	
	public void pressRight(boolean pressed){
		this.right = pressed;
		repaint();
	}
	
	private void createArrows(){
		int cX = dim.width/2;
		int cY = dim.height/2;
		//UP
		upX 		= new int[]{(int) (cX+0.08*dim.width),  (int) (cX+0.08*dim.width),  (int) (cX+0.2*dim.width), cX,                                        (int) (cX-0.2*dim.width),   (int) (cX-0.08*dim.width),  (int) (cX-0.08*dim.width)};
		upY 		= new int[]{(int) (cY-0.1*dim.height), 	(int) (cY-0.35*dim.height),	(int) (cY-0.35*dim.height), (int) (cY-0.5*dim.height), (int) (cY-0.35*dim.height), (int) (cY-0.35*dim.height), (int) (cY-0.1*dim.height)};
		//DOWN
		downX 	= new int[]{(int) (cX+0.08*dim.width), (int) (cX+0.08*dim.width),   (int) (cX+0.2*dim.width), cX,                                        (int) (cX-0.2*dim.width),   (int) (cX-0.08*dim.width),  (int) (cX-0.08*dim.width)};
		downY 	= new int[]{(int) (cY+0.1*dim.height), 	(int) (cY+0.35*dim.height),	(int) (cY+0.35*dim.height), (int) (cY+0.5*dim.height), (int) (cY+0.35*dim.height), (int) (cY+0.35*dim.height), (int) (cY+0.1*dim.height)};
		//LEFT
		leftY 		= new int[]{(int) (cY+0.08*dim.height), (int) (cY+0.08*dim.height),   (int) (cY+0.2*dim.height), cY,                                        (int) (cY-0.2*dim.height),   (int) (cY-0.08*dim.height),  (int) (cY-0.08*dim.height)};
		leftX 		= new int[]{(int) (cX-0.1*dim.width), 	(int) (cX-0.35*dim.width),	(int) (cX-0.35*dim.width), (int) (cX-0.5*dim.width), (int) (cX-0.35*dim.width), (int) (cX-0.35*dim.width), (int) (cX-0.1*dim.width)};
		//RIGHT
		rightY 	= new int[]{(int) (cY+0.08*dim.height), (int) (cY+0.08*dim.height),   (int) (cY+0.2*dim.height), cY,                                        (int) (cY-0.2*dim.height),   (int) (cY-0.08*dim.height),  (int) (cY-0.08*dim.height)};
		rightX 	= new int[]{(int) (cX+0.1*dim.width), 	(int) (cX+0.35*dim.width),	(int) (cX+0.35*dim.width), (int) (cX+0.5*dim.width), (int) (cX+0.35*dim.width), (int) (cX+0.35*dim.width), (int) (cX+0.1*dim.width)};
	}
	
	@Override
	public void paint(Graphics g){
		g.clearRect(0, 0, dim.width, dim.height);
		g.setColor(Color.RED);
		if(up){
			g.fillPolygon(upX, upY, 7);
		}else{
			g.drawPolygon(upX, upY, 7);
		}
		if(down){
			g.fillPolygon(downX, downY, 7);
		}else{
			g.drawPolygon(downX, downY, 7);
		}
		if(left){
			g.fillPolygon(leftX, leftY, 7);
		}else{
			g.drawPolygon(leftX, leftY, 7);
		}
		if(right){
			g.fillPolygon(rightX, rightY, 7);
		}else{
			g.drawPolygon(rightX, rightY, 7);
		}
	}

}
