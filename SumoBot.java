
import lejos.nxt.*;

/**
 *Totally unique and not at all inspired by bumperCar implementation of a sumo robot
 * @author Troels and Kasper and Frederik
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

//behavior for scouting out other robots
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
    //turn around, the detectEnemy behavior runs a sensor that will detect anything in front
    Motor.A.forward();
    Motor.C.backward();
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

//behavior to detect enemies and drive towards them
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
  
  //the ultrasonic sensor is run in a thread so no method needs to wait for it to get a response
  public void run()
  {
    while ( true ) distance = sonar.getDistance();
  }

  //high priority when there is an enemy in front, lower when it is driving. This was a mistake
  //since the robot would keep driving until reaching the end instead of reorienting itself towards
  //the direction of the enemy.
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
	
    // CHARGE the enemy with a suitable warcry
    LCD.clear();
    LCD.drawString("BLOOD FOR THE",0,1);
    LCD.drawString("BLOOD GOD!",0,2);
    LCD.drawString("SKULLS FOR HIS",0,4);
    LCD.drawString("SKULL THRONE!",0,5);
    Motor.A.forward();
    Motor.C.forward();
    //we were going to put sawblades on motor b, but these didn't make it into the final build
    Motor.B.forward();
    while (!_suppressed )
    {
       Thread.yield(); //don't exit till suppressed
    }
   
    
    Motor.A.stop(); 
    Motor.C.stop();
    LCD.drawString("Stopped       ",0,3);
    active = false;
    
  }
  private UltrasonicSensor sonar;
}

//behavior for backing up if front lightsensor detects the white sensor
class AvoidEdgeFront implements Behavior
{
	private boolean active = false;
	 private boolean _suppressed = false;
	 
	 public AvoidEdgeFront()
	 {
	 front = new LightSensor(SensorPort.S1);
	 }
	 //will get highest priority while on the edge, may be interupted by detectEnemy while driving away from the edge
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
		
	 // Backward for 1000 msec
	    LCD.clear();
	    LCD.drawString("METAL BAWXES",0,3);
	    LCD.drawInt(front.getLightValue(),0,5);
	    Motor.A.backward();
	    Motor.C.backward();
	    
	    int now = (int)System.currentTimeMillis();
	    while (!_suppressed && ((int)System.currentTimeMillis()< now + 1000) )
	    {
	       Thread.yield(); //don't exit till suppressed
	    }
	    // Turn
	    LCD.clear();
	    LCD.drawString("REEEEEEEEEE",0,3);
	    Motor.A.rotate(180, true);// start Motor.A rotating forward
	    Motor.C.rotate(-180, true);  // rotate C opposite to make the turn
	    while (!_suppressed && Motor.C.isMoving())
	    {
	      Thread.yield(); //don't exit till suppressed
	    }
	    
	    Motor.A.stop(); 
	    Motor.C.stop();
	    LCD.drawString("STOP FOR NOTHING",0,3);
	    active = false;
	  }
	  private LightSensor front;
	}
	
//do the same for the back sensor
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
	    // forward for 1000 msec
	    LCD.clear();
	    LCD.drawString("CHAAAARGE!",0,3);
	    LCD.drawInt(back.getLightValue(),0,5);

	    Motor.A.forward();
	    Motor.C.forward();
	    int now = (int)System.currentTimeMillis();
	    while (!_suppressed && ((int)System.currentTimeMillis()< now + 1000) )
	    {
	       Thread.yield(); //don't exit till suppressed
	    }
	   
	    Motor.A.stop(); 
	    Motor.C.stop();
	    LCD.drawString("Stopped       ",0,3);
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


