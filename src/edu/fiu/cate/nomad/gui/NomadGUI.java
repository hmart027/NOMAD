package edu.fiu.cate.nomad.gui;

import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
//import java.io.OutputStream;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import joystick.DS4;
import joystick.Gamepad;
import joystick.PollEventListener;

import com.protocol.Protocol;

import comm.serial.Comm;
//import comm.serial.InputStreamListener;
import edu.fiu.cate.nomad.control.Base;
import edu.fiu.cate.nomad.control.BaseRasp;
import edu.fiu.cate.nomad.control.NomadBase;

public class NomadGUI extends JFrame{
	private static final long serialVersionUID = -8206042273607451553L;
	
	public static final byte[] HB_PACKET = Protocol.pack(Base.HB_PACKET);
	public static final byte[] ENCODER_REQ_PACKET = Protocol.pack(Base.ENCODER_REQ_PACKET);
	public static final byte[] VELOCITY_REQ_PACKET = Protocol.pack(Base.VELOCITY_REQ_PACKET);
	
	private JPanel contentPane 		= new JPanel();
	private JMenuBar menuBar 		= new JMenuBar();
	
	DirectionalPad pad;
	
//	private Comm com = new Comm();
//	private static int    NOMAD_BAUD = 57600;
//	private volatile boolean msgSent = false;
//	private Protocol protocol = new Protocol();
//	private NomadBase base = new NomadBase();
	private BaseRasp base = new BaseRasp();
	
	private DS4 joystick = DS4.getJoystick(0, 10, true);
	
	public boolean excecuteAutoRutine = false;
	
	public NomadGUI(){
		setTitle("CATE Lab's NOMAD");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		GraphicsDevice gd = getLargestResolutionDisplay();
		int w = (int) (gd.getDisplayMode().getWidth()*0.9);
		int h = (int) (gd.getDisplayMode().getHeight()*0.9);
		int n = (w<h)? w: h;
		
		java.awt.Rectangle bounds = gd.getDefaultConfiguration().getBounds();
		setBounds(bounds.x+bounds.width/2, bounds.y, n, n);
		contentPane.setPreferredSize(getContentPane().getPreferredSize());
		contentPane.setBackground(java.awt.Color.BLACK);
		setContentPane(contentPane);

		menuBar.add(new SerialPortMenu());
		menuBar.add(new RoutineMenu());
		this.setJMenuBar(menuBar);
		
//		com.addInputStreamListener(new BaseListener());
		
		KeyCommand keys = new KeyCommand();
		contentPane.addKeyListener(keys);
		contentPane.setFocusable(true);
		
		pad = new DirectionalPad(new Dimension(500, 500));
		pad.addKeyListener(keys);
		contentPane.add(pad);
		
		if(joystick!=null)
			joystick.setPollEventListener(new PollEventListener() {
				@Override
				public void onPoll(Gamepad gamepad) {	
					DS4 joystick = (DS4) gamepad;
					double ax = joystick.getLX();
					double ay = joystick.getLY();
					double az = joystick.getRX();	
					System.out.println("AX: "+ax);
					//System.out.println("Rot: "+az);
	
					if(joystick.isTriggerPressed()){
						excecuteAutoRutine = false;
					}
					
					if(!excecuteAutoRutine){
						base.setWheelPWM((int)(-ay*100));
						base.setTurnPWM((int)(-ax*100));
						//base.setTurretPWM((int)(-az*100));
						base.setTurretPWM(0);
//						if(com.isConnected()){
//							byte[] msg = Protocol.pack(Base.getBaseMotorsPWMMessage());
//							com.sendByteArray(msg);
//							msgSent = true;
//						}
					}
				}
			});
		
		setVisible(true);
	}
		
	public GraphicsDevice getLargestResolutionDisplay(){
		GraphicsDevice gd = null;
		double r = 0;
		for(GraphicsDevice g : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()){
			double rt = Math.sqrt(Math.pow(g.getDisplayMode().getWidth(), 2) + Math.pow(g.getDisplayMode().getHeight(), 2));
			if(rt>=r){
				gd = g;
				r = rt;
			}
		}
		return gd;
	}

	public class SerialPortMenu extends JMenu{
		
		private static final long serialVersionUID = -5308600274706603186L;
		private java.util.TreeMap<String, JMenuItem> ports = new java.util.TreeMap<>();
		private JMenuItem refresh 		= new JMenuItem("Refresh");
		private JMenuItem disconnect 	= new JMenuItem("Disconnect");
		
		public SerialPortMenu(){
			this.setText("Serial Port");
			refresh.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					refresh();
				}
			});
			disconnect.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
	//				com.closeComm();
	//				base.disconnect();
					for(JMenuItem i: ports.values())
						i.setEnabled(true);
					disconnect.setEnabled(false);
					refresh.setEnabled(true);
					refresh();
				}
			});
			disconnect.setEnabled(false);
			refresh();
		}
		
		private void refresh(){
			this.removeAll();
			java.util.ArrayList<String> portList = Comm.getSerialPortList();
			for(String c: portList){
				if(!ports.containsKey(c)){
					JMenuItem item = new JMenuItem(c);
					item.addActionListener(new ActionListener() {			
						@Override
						public void actionPerformed(ActionEvent e) {
							String txt = ((JMenuItem)e.getSource()).getText();
//							com.closeComm();
//							if(com.getComm( txt, NOMAD_BAUD)){
//								System.out.println("Connected to: "+txt+",  "+NOMAD_BAUD);
//								new KeepAliveHeartbeat(com.getOutputStream()).start();
//								new EncoderDataRequest().start();
//							}	
//							base.disconnect();
//							if(base.connectTo( txt)){
//								System.out.println("Connected to: "+txt);
//							}	
							((JMenuItem)e.getSource()).setEnabled(false);
							refresh.setEnabled(false);
							disconnect.setEnabled(true);
						}
					});
					ports.put(c, item);
				}
				for(String s: ports.keySet()){
					if(!portList.contains(s))
						ports.remove(s);
					else
						this.add(ports.get(s));
				}
			}
			this.add(refresh);
			this.add(disconnect);
		}
		
	}
	
	public class RoutineMenu extends JMenu{
		
		private static final long serialVersionUID = -1490951096370569562L;

		public RoutineMenu(){
			this.setText("Routines");
			JMenuItem r1 = new JMenuItem("Simple turn");
			r1.addActionListener(new ActionListener() {		
				@Override
				public void actionPerformed(ActionEvent arg0) {
					excecuteAutoRutine = true;
					new SmallRoutine().start();
				}
			});
			this.add(r1);
		}
	}
	
/*	public class BaseListener implements InputStreamListener{

		@Override
		public void onByteReceived(int d) {
			if(protocol.parseChar((char)d))
				Base.processPayload(protocol.getPayload());
		}
		
	}
	
	public class KeepAliveHeartbeat extends Thread{
		OutputStream out;
		KeepAliveHeartbeat( OutputStream o){
			out = o;
		}
		public void run(){
			while(com.isConnected()){
				try {
					if(!msgSent)
						com.sendByteArray(HB_PACKET);
					else
						msgSent = false;
					Thread.sleep(400);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public class EncoderDataRequest extends Thread{
		public void run(){
			while(com.isConnected()){
				try {
					Thread.sleep(500);
//					com.sendByteArray(ENCODER_REQ_PACKET, 10);
//					com.sendByteArray(VELOCITY_REQ_PACKET, 10);
//					msgSent = true;
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}*/
	
	public class KeyCommand implements KeyListener{
		boolean[] keyMap = new boolean[128];
		
		@Override
		public void keyPressed(KeyEvent e) {
			if(!keyMap[e.getKeyCode()]){
				keyMap[e.getKeyCode()] = true;
				System.out.println("P: "+e.getKeyChar());
				switch(e.getKeyChar()){
				case 'w':
					base.setWheelPWM(50);
					pad.pressUP(true);
					break;
				case 's':
					base.setWheelPWM(-50);
					pad.pressDown(true);
					break;
				case 'a':
					base.setTurnPWM(50);
					pad.pressLeft(true);
					break;
				case 'd':
					base.setTurnPWM(-50);
					pad.pressRight(true);
					break;
				}
//				com.sendByteArray(Protocol.pack(Base.getBaseMotorsPWMMessage()));
//				msgSent = true;
			}
		}

		@Override
		public void keyReleased(KeyEvent e) {
			System.out.println("R: "+e.getKeyChar());
			keyMap[e.getKeyCode()] = false;
			switch(e.getKeyChar()){
			case 'w':
				base.setWheelPWM(0);
				pad.pressUP(false);
				break;
			case 's':
				base.setWheelPWM(0);
				pad.pressDown(false);
				break;
			case 'a':
				base.setTurnPWM(0);
				pad.pressLeft(false);
				break;
			case 'd':
				base.setTurnPWM(0);
				pad.pressRight(false);
				break;
			default:
				System.out.println("Default");
				base.setWheelPWM(0);
					
			}
//			if(com.isConnected()){
//				byte[] msg = Protocol.pack(Base.getBaseMotorsPWMMessage());
//				com.sendByteArray(msg);
//				msgSent = true;
//			}
		}

		@Override
		public void keyTyped(KeyEvent e) {
		}
	}

	public class SmallRoutine extends Thread{
		
		public void run(){

//			while(excecuteAutoRutine){
//				Base.setWheelPWM((int)(50));
//				Base.setTurnPWM((int)(25));
//				if(com.isConnected() && excecuteAutoRutine){
//					byte[] msg = Protocol.pack(Base.getBaseMotorsPWMMessage());
//					com.sendByteArray(msg);
//					msgSent = true;
//				}
//			}
		}
	}
}
