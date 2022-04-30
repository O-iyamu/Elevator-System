/**
 * 
 */

import java.util.ArrayList;
import java.util.List;

/**
 * Class FloorLamp represents the lamp that is on each floor.
 * @author Ryan
 *
 */
public class FloorLamp {
	private List<Integer> elevators;
	
	/**
	 * Constructor for the FloorLamp. Creates an ArrayList of elevators.
	 */
	public FloorLamp() {
		elevators = new ArrayList<>();
	}
	
	/**
	 * Used to update the ArrayList of elevators with information about the corresponding lamp.
	 * @param elevators
	 */
	public void update(List<Integer> elevators) {
		this.elevators = elevators;
	}
	
	/**
	 * Used to see if the lamp is currently on.
	 * 
	 * @param id
	 * @return
	 */
	public boolean isItOn(int id) {
		if(elevators.contains(id)){
			return true;
		}
		return false;
	}
}
