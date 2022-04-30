package project;

/**
 * Class used to imitate when a button has been pressed on the elevator.
 * 
 * @author Colton
 *
 */
public class ElevatorButton {

	private int elevator;
	private boolean buttonPressed;
	
	/*
	 * Elevator button contructor.
	 */
	public ElevatorButton(int elevator, boolean buttonPressed)
	{
		this.elevator = elevator;
		this.buttonPressed = buttonPressed;
	}
	
	/*
	 * Sets button pressed to true, and after 3 seconds returns it to false;
	 */
	public void elevatorButtonPressed()
	{
		buttonPressed = true;
		System.out.println("Elevator "+elevator+": button pressed.");
		
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		buttonPressed = false;
	}
	
	
}
