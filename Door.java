
/**
 * Class Door is used for simulating the doors of each individual elevator.
 * 
 * @author Colton
 *
 */
public class Door {
	
	private int elevator;
	private boolean doorOpen = false;
	private boolean doorStuck = false;
	
	/**
	 * Constructor for Door class. The door is given default values by the Elevator class.
	 * 
	 * @param elevator
	 * @param doorOpen
	 * @param doorStuck
	 */
	public Door(int elevator, boolean doorOpen, boolean doorStuck) 
	{
		this.elevator = elevator;
		this.doorOpen = doorOpen;
		this.doorStuck = doorStuck;
	}
	
	/**
	 * Open the door of the elevator.
	 */
	public void open()
	{
		doorOpen = true;
		System.out.println("Elevator "+elevator+": Door opened.");
	}
	
	/**
	 * Close the door of the elevator.
	 */
	public void close()
	{
		doorOpen = false;
		System.out.println("Elevator "+elevator+": Door closed.");
	}
	
	/**
	 * Getter for doorStuck boolean to see if the door is stuck.
	 * 
	 * @return boolean doorStuck
	 */
	public boolean isStuck()
	{
		return doorStuck;
	}
	
}
