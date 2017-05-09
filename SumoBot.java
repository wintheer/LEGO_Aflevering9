
import lejos.nxt.*;

/**
 * Demonstration of the Behavior subsumption classes.
 * 
 * Requires a wheeled vehicle with two independently controlled
 * motors connected to motor ports A and C, and 
 * a touch sensor connected to sensor  port 1 and
 * an ultrasonic sensor connected to port 3;
 * 
 * @author Brian Bagnall and Lawrie Griffiths, modified by Roger Glassey
 *
 * Uses a new version of the Behavior interface and Arbitrator with
 * integer priorities returned by takeCaontrol instead of booleans.
 * 
 * Exit behavior inserted, local distance sampling thread and
 * backward drive added in DetectWall by Ole Caprani, 23-4-2012
 */
public class SumoBot
{

  public static void main(String[] args)
  {
	
	  
    Motor.A.setSpeed(400);
    Motor.C.setSpeed(400);
    Behavior b1 = new LookAround();
    Behavior b2 = new DetectEnemy();
    Behavior b3 = new AvoidEdgeFront();
    Behavior b4 = new AvoidEdgeBack();

    Behavior b5 = new Exit();
    Behavior[] behaviorList =
    {
      b1, b2, b3, b4, b5
    };
    Arbitrator arbitrator = new Arbitrator(behaviorList);
    LCD.drawString("KHORNEBOT 40000",0,1);
   
    Button.ENTER.waitForPress();
    try {
		Thread.sleep(3000);
	} catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    arbitrator.start();
  }
}


class LookAround implements Behavior
{

  private boolean _suppressed = false;

  public int takeControl()
  {
    return 10;  // this behavior always wants control.
  }

  public void suppress()
  {
    _suppressed = true;// standard practice for suppress methods
  }

  public void action()
  {
    _suppressed = false;
    Motor.A.forward();
    Motor.C.backward();
    
    Motor.B.forward();
    LCD.clear();
    LCD.drawString("WHO HIDES",0,1);
    LCD.drawString("FROM KHORNE?!",0,2);

    while (!_suppressed)
    {
      Thread.yield(); //don't exit till suppressed
    }
    Motor.A.stop(); // not strictly necessary, but good programming practice
    Motor.C.stop();
    LCD.drawString("Drive stopped",0,2);
  }
}


class DetectEnemy extends Thread implements Behavior
{
  private boolean _suppressed = false;
  private boolean active = false;
  private int distance = 255;

  public DetectEnemy()
  {
    sonar = new UltrasonicSensor(SensorPort.S3);
    this.setDaemon(true);
    this.start();
  }
  
  public void run()
  {
    while ( true ) distance = sonar.getDistance();
  }

  public int takeControl()
  {
    if (distance < 40)
       return 80;
    if ( active )
       return 40;
    return 0;
  }

  public void suppress()
  {
    _suppressed = true;// standard practice for suppress methods  
  }

  public void action()
  {
    _suppressed = false;
    active = true;
   // Sound.beepSequenceUp();
	
    // CHARGE
    LCD.clear();
    LCD.drawString("BLOOD FOR THE",0,1);
    LCD.drawString("BLOOD GOD!",0,2);
    LCD.drawString("SKULLS FOR HIS",0,4);
    LCD.drawString("SKULL THRONE!",0,5);
    Motor.A.forward();
    Motor.C.forward();
    Motor.B.forward();
    while (!_suppressed )
    {
       Thread.yield(); //don't exit till suppressed
    }
   
    
    Motor.A.stop(); 
    Motor.C.stop();
    LCD.drawString("Stopped       ",0,3);
   // Sound.beepSequence();
    active = false;
    
  }
  private UltrasonicSensor sonar;
}

class AvoidEdgeFront implements Behavior
{
	private boolean active = false;
	 private boolean _suppressed = false;
	 
	 public AvoidEdgeFront()
	 {
	 front = new LightSensor(SensorPort.S1);
	 }
	  public int takeControl()
	  {
	    if (front.getLightValue() > 50)
	       return 100;
	    if ( active )
	       return 50;
	    return 0;
	  }

	  public void suppress()
	  {
	    _suppressed = true;// standard practice for suppress methods  
	  }

	  public void action()
	  {
	    _suppressed = false;
	    active = true;
	    //Sound.beepSequenceUp();
		
	 // Backward for 1000 msec
	    LCD.clear();
	    LCD.drawString("METAL BAWXES",0,3);
	    LCD.drawInt(front.getLightValue(),0,5);
	    Motor.A.backward();
	    Motor.C.backward();
	    
	    Motor.B.forward();
	    int now = (int)System.currentTimeMillis();
	    while (!_suppressed && ((int)System.currentTimeMillis()< now + 1000) )
	    {
	       Thread.yield(); //don't exit till suppressed
	    }
	   
	    
	    // Turn
	    LCD.clear();
	    LCD.drawString("REEEEEEEEEE",0,3);
	    Motor.A.rotate(180, true);// start Motor.A rotating backward
	    Motor.C.rotate(-180, true);  // rotate C farther to make the turn
	    while (!_suppressed && Motor.C.isMoving())
	    {
	      Thread.yield(); //don't exit till suppressed
	    }
	    
	    Motor.A.stop(); 
	    Motor.C.stop();
	    LCD.drawString("STOP FOR NOTHING",0,3);
	  //  Sound.beepSequence();
	    active = false;
	    
	  }
	  private LightSensor front;

	}
	

class AvoidEdgeBack implements Behavior
{
	  private boolean _suppressed = false;
	  private boolean active = false;
	 
	  public AvoidEdgeBack(){
	  back = new LightSensor(SensorPort.S2);
	  }
	  public int takeControl()
	  {
	    if ( back.getLightValue() > 50)
	       return 100;
	    if ( active )
	       return 50;
	    return 0;
	  }

	  public void suppress()
	  {
	    _suppressed = true;// standard practice for suppress methods  
	  }

	  public void action()
	  {
	    _suppressed = false;
	    active = true;
	//    Sound.beepSequenceUp();
		
	    // Backward for 1000 msec
	    LCD.clear();
	    LCD.drawString("CHAAAARGE!",0,3);
	    LCD.drawInt(back.getLightValue(),0,5);

	    Motor.A.forward();
	    Motor.C.forward();
	    
	    Motor.B.forward();
	    int now = (int)System.currentTimeMillis();
	    while (!_suppressed && ((int)System.currentTimeMillis()< now + 1000) )
	  
	    {
	       Thread.yield(); //don't exit till suppressed
	    }
	   
	    
	    Motor.A.stop(); 
	    Motor.C.stop();
	    LCD.drawString("Stopped       ",0,3);
	   // Sound.beepSequence();
	    active = false;
	    
	  }
	  private LightSensor back;

	}
	

class Exit implements Behavior
{
  private boolean _suppressed = false;

  public int takeControl()
  {
    if ( Button.ESCAPE.isPressed() )
    	return 200;
    return 0;
  }

  public void suppress()
  {
    _suppressed = true;// standard practice for suppress methods  
  }

  public void action()
  {
    System.exit(0);
  }
}


