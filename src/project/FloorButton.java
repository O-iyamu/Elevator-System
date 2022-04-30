/**
 * 
 */
package project;

/**
 * @author Ryan
 *
 */
public class FloorButton {
	private boolean isOn;
	private boolean up;
	
	public FloorButton() {
		isOn = false;
		up = false;
	}
	
	public void turnOn(boolean up) {
		isOn = true;
		this.up = up;
	}
	
	public void turnOff() {
		isOn = false;
	}
	
	public boolean getIsOn() {
		 return isOn;
	}
	
	public boolean getUp() {
		return up;
	}
}
