/**
 * 
 */

/**
 * Class FloorButton represents the button for each floor.
 * @author Ryan
 *
 */
public class FloorButton {
	private boolean isOn;
	private boolean up;
	
	/**
	 * Constructor for FloorButton. Values are defaulted to false.
	 */
	public FloorButton() {
		isOn = false;
		up = false;
	}
	
	/**
	 * Used to turn on a button, returns true.
	 * 
	 * @param up
	 */
	public void turnOn(boolean up) {
		isOn = true;
		this.up = up;
	}
	
	/**
	 * Used to turn off a button.
	 */
	public void turnOff() {
		isOn = false;
	}
	
	/**
	 * Get function to see if the button is on or off.
	 * @return
	 */
	public boolean getIsOn() {
		 return isOn;
	}
	
	/**
	 * Get function to see if the direction is up.
	 * @return
	 */
	public boolean getUp() {
		return up;
	}
}
