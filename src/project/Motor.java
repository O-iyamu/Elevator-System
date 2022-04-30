package project;

/**
 * Class Motor is used to simulate the use of a motor for each individual elevator.
 * 
 * @author Colton
 *
 */
public class Motor {

	private int elevator;
	private boolean goingUp;
	private boolean motorTurnedOn;
	
	public Motor(int elevator, boolean goingUp, boolean isMoving)
	{
		this.elevator = elevator;
		this.goingUp = goingUp;
		this.motorTurnedOn = isMoving;
	}
	
	/*
	 * Set elevator to go up. 
	 */
	public void elevatorGoingUp()
	{
		motorTurnedOn = true;
		goingUp = true;
		System.out.println("Elevator "+elevator+": motor activated. Going up.");
	}
	
	/*
	 * Set elevator to go down. 
	 */
	public void elevatorGoingDown()
	{
		motorTurnedOn = true;
		goingUp = false;
		System.out.println("Elevator "+elevator+": motor activated. Going down.");
	}
	
	/*
	 * Turn the motor off.
	 */
	public void elevatorArrived()
	{
		motorTurnedOn = false;
		System.out.println("Elevator "+elevator+": motor deactivated.");
	}

	public boolean isGoingUp() {
		return goingUp;
	}

	public boolean isMotorTurnedOn() {
		return motorTurnedOn;
	}
}
