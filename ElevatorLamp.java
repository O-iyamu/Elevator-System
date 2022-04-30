
/**
 * Class ElevatorLamp is used to imitate the lamps within an elevator that lights
 * up when an elevator has been called.
 * 
 * @author Colton
 *
 */
public class ElevatorLamp {
	
	private int elevator;
	private boolean lampOn;
	
	/*
	 * Constructor for Class ElevatorLamp. Default values given by the corresponding Elevator.
	 */
	public ElevatorLamp(int elevator, boolean lampOn)
	{
		this.elevator = elevator;
		this.lampOn = lampOn;
	}
	
	/*
	 * Function used to turn on the elevator lamp.  
	 */
	public void turnElevatorLampOn()
	{
		lampOn = true;
	}
	
	/*
	 * Function used to turn off the elevator lamp.
	 */
	public void turnElevatorLampOff()
	{
		lampOn = false;
	}
}
