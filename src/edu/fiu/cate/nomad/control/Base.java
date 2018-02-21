package edu.fiu.cate.nomad.control;

public class Base {
	
	public static final byte[] HB_PACKET = {0};
	public static final byte[] ENCODER_REQ_PACKET = {10};
	public static final byte[] VELOCITY_REQ_PACKET = {11};
	public static final byte[] IR_REQ_PACKET = {20};

	protected static volatile boolean connected = false;
		
	protected static double wheelPWM, turnPWM, turretPWM;
	protected static int wheelEncoderCount, turningEncoderCount, turretEncoderCount;
	
	protected static double wheelSpeed, turningSpeed, turretSpeed;
	protected static double baseAngle, turretAngle;
	
	protected static int[] irSensors = new int[16];
	
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
		byte whee, turn, turr;
		whee = (byte) wheelPWM;
		turn = (byte) turnPWM;
		turr = (byte) turretPWM;
		if(whee<0){
			whee*=-1;
			whee=(byte) (whee|0b10000000);
		}
		if(turn<0){
			turn*=-1;
			turn=(byte) (turn|0b10000000);
		}
		if(turr<0){
			turr*=-1;
			turr=(byte) (turr|0b10000000);
		}
		return new byte[]{1, whee, turn, turr};
	}
	
	long lt = 0;
	public static boolean processPayload(byte[] payload){
		System.out.println("Good Msg");
//		long ct = System.currentTimeMillis();
//		System.out.println("Dt: "+(ct-lt));
//		lt = ct;
//		processHeartBeat();
		switch(payload[0]){
		case 0:{		//Heartbeat
//			System.out.println("HB: "+(System.currentTimeMillis()-lt));
//			lt = System.currentTimeMillis();
			System.out.println("HB");
			return true;
		}
		case 10:{		//Motors Location (0 - 360)
//			//MSB first
//			wheelEncoderCount 		= ((int)payload[1])<<24 | (((int)payload[2])&0x0FF)<<16 | (((int)payload[3])&0x0FF)<<8 | (((int)payload[4])&0x0FF);
//			turningEncoderCount		= ((int)payload[5])<<24 | (((int)payload[6])&0x0FF)<<16 | (((int)payload[7])&0x0FF)<<8 | ((int)payload[8])&0x0FF;
//			turretEncoderCount		= ((int)payload[9])<<24 | (((int)payload[10])&0x0FF)<<16 | (((int)payload[11])&0x0FF)<<8 | ((int)payload[12])&0x0FF;
			//LSB first
			wheelEncoderCount 		= ((int)payload[4])<<24 | (((int)payload[3])&0x0FF)<<16 | (((int)payload[2])&0x0FF)<<8 | (((int)payload[1])&0x0FF);
			turningEncoderCount		= ((int)payload[8])<<24 | (((int)payload[7])&0x0FF)<<16 | (((int)payload[6])&0x0FF)<<8 | ((int)payload[5])&0x0FF;
			turretEncoderCount		= ((int)payload[12])<<24 | (((int)payload[11])&0x0FF)<<16 | (((int)payload[10])&0x0FF)<<8 | ((int)payload[9])&0x0FF;
//			System.out.println("Enc: "+wheelEncoderCount +", "+ turningEncoderCount +", "+ turretEncoderCount);
			int max = 135700;
			double w = 360d/(double)max;
			System.out.println("Enc: "+ turningEncoderCount*w +", "+ turretEncoderCount*w);
			
			return true;
		}
		case 11:{		//Motors Velocities (counts/ms)
			int wheelVel 	= ((int)payload[4])<<24 | (((int)payload[3])&0x0FF)<<16 | (((int)payload[2])&0x0FF)<<8 | (((int)payload[1])&0x0FF);
			int turnVel		= ((int)payload[8])<<24 | (((int)payload[7])&0x0FF)<<16 | (((int)payload[6])&0x0FF)<<8 | ((int)payload[5])&0x0FF;
			int turretVel	= ((int)payload[12])<<24 | (((int)payload[11])&0x0FF)<<16 | (((int)payload[10])&0x0FF)<<8 | ((int)payload[9])&0x0FF;
			System.out.println(wheelVel+", "+turnVel+", "+turretVel);
			return true;
		}
		case 20:{ // IR sensor readings
			for(int i=0; i<16; i++){
				irSensors[i] = (((int)payload[2+i*2])&0x0FF)<<8 | (((int)payload[1+i*2])&0x0FF);
//				if(i==5){
//					System.out.println("IR: "+Integer.toHexString(payload[12]&0x0FF)+", "+Integer.toHexString(payload[11]&0x0FF));
//				}
			}
			return true;
		}
		case 21:{
			System.out.println("OBS: "+payload[1]);
			return true;
		}
		default:{
			System.out.println("Unknown msg: "+payload[0]);
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
