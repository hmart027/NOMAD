package edu.fiu.cate.nomad.control;

public class Base {
	
	public static final byte[] HB_PACKET = {0};
	public static final byte[] ENCODER_REQ_PACKET = {10};
	public static final byte[] VELOCITY_REQ_PACKET = {11};

	protected static volatile boolean connected = false;
		
	protected static double wheelPWM, turnPWM, turretPWM;
	protected static int wheelEncoderCount, turningEncoderCount, turretEncoderCount;
	
	protected static double wheelSpeed, turningSpeed, turretSpeed;
	protected static double baseAngle, turretAngle;
	
	protected static HeartBeatTimer hbTimer = new HeartBeatTimer();
	
	protected Base(){}
		
	public static void setWheelPWM(int wheel){
		wheelPWM = wheel;
	}
	
	public static void setTurnPWM(int turn){
		turnPWM = turn;
	}
	
	public static void setTurretPWM(int turret){
		turretPWM = turret;
	}
	
	public static byte[] getBaseMotorsPWMMessage(){
		return new byte[]{1, (byte) wheelPWM, (byte) turnPWM, (byte) turretPWM};
	}
	
	long lt = 0;
	public static boolean processPayload(byte[] payload){
//		System.out.println("Good Msg");
//		long ct = System.currentTimeMillis();
//		System.out.println("Dt: "+(ct-lt));
//		lt = ct;
		processHeartBeat();
		switch(payload[0]){
		case 0:{		//Heartbeat
//			System.out.println("HB: "+(System.currentTimeMillis()-lt));
//			lt = System.currentTimeMillis();
			return true;
		}
		case 10:{		//Motors Location (0 - 360)
			wheelEncoderCount 		= ((int)payload[4])<<24 | (((int)payload[3])&0x0FF)<<16 | (((int)payload[2])&0x0FF)<<8 | (((int)payload[1])&0x0FF);
			turningEncoderCount		= ((int)payload[8])<<24 | (((int)payload[7])&0x0FF)<<16 | (((int)payload[6])&0x0FF)<<8 | ((int)payload[5])&0x0FF;
			turretEncoderCount		= ((int)payload[12])<<24 | (((int)payload[11])&0x0FF)<<16 | (((int)payload[10])&0x0FF)<<8 | ((int)payload[9])&0x0FF;
			System.out.println(wheelEncoderCount +", "+ turningEncoderCount +", "+ turretEncoderCount);
			return true;
		}
		case 11:{		//Motors Velocities (counts/ms)
			int wheelVel 	= ((int)payload[4])<<24 | (((int)payload[3])&0x0FF)<<16 | (((int)payload[2])&0x0FF)<<8 | (((int)payload[1])&0x0FF);
			int turnVel		= ((int)payload[8])<<24 | (((int)payload[7])&0x0FF)<<16 | (((int)payload[6])&0x0FF)<<8 | ((int)payload[5])&0x0FF;
			int turretVel	= ((int)payload[12])<<24 | (((int)payload[11])&0x0FF)<<16 | (((int)payload[10])&0x0FF)<<8 | ((int)payload[9])&0x0FF;
			System.out.println(wheelVel+", "+turnVel+", "+turretVel);
			return true;
		}
		} //90de - 33,639
		return false;
	}
	
	public static void processHeartBeat(){
		if(!connected)
			System.out.println("Connection");
		connected=true;
		hbTimer.kill();
		hbTimer = new HeartBeatTimer();
		hbTimer.start();
	}
	
	protected static class HeartBeatTimer extends Thread{	
		private volatile boolean kill = false;
		public void kill(){
			kill = true;
		}
		public void run(){
			try {
				Thread.sleep(1000);
				if(!kill){
					connected = false;
					System.out.println("Connection Lost");
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
