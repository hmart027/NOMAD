package edu.fiu.cate.nomad.video.streaming;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import edu.fiu.cate.nomad.gui.binocular.CameraMenu;
import edu.fiu.cate.nomad.gui.binocular.ImagePanel;
import java.awt.FlowLayout;

public class StereoView extends JFrame{

	private static final long serialVersionUID = -318211885027959278L;
	
	private JMenuBar menuBar = new JMenuBar();
	private JMenu settingsMenu = new JMenu("Settings");
	private CameraMenu cameraMenu = new CameraMenu();
	
	private JPanel contentPane;
	private ImagePanel rightImage;
	private ImagePanel leftImage;

	public StereoView(){
		
		setTitle("Stereo View");
		setBounds(0, 0, 500, 500);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		setJMenuBar(menuBar);
		menuBar.add(settingsMenu);
		JMenuItem cameraMenu = new JMenuItem("Cameras");
		cameraMenu.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				StereoView.this.cameraMenu.setVisible(true);				
			}
		});
		settingsMenu.add(cameraMenu);

		contentPane = new ContentPane();
		setContentPane(contentPane);
		
		contentPane.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		contentPane.add(leftImage);
		contentPane.add(rightImage);
		pack();
		setVisible(true);
	}

	public void setRightImage(BufferedImage img){
		rightImage.setImage(img);
	}
	
	public void setLeftImage(BufferedImage img){
		leftImage.setImage(img);
	}
	
	private class ContentPane extends JPanel{
			
		private static final long serialVersionUID = 7177824075739447895L;
		
		public ContentPane(){
			rightImage = new ImagePanel(new Dimension(600, 400));
			leftImage = new ImagePanel(new Dimension(600, 400));
		}

		@Override
		public void setBounds(int x, int y, int width, int height) {
			super.setBounds(x, y, width, height);
			Dimension dim = new Dimension(width/2-10, height-10);
			leftImage.setPreferredSize(dim);
			rightImage.setPreferredSize(dim);
		}	
		
	}
	
}
