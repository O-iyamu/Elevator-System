/**
 * 
 */
package project;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ryan
 *
 */
public class FloorLamp {
	private List<Integer> elevators;
	
	public FloorLamp() {
		elevators = new ArrayList<>();
	}
	
	public void update(List<Integer> elevators) {
		this.elevators = elevators;
	}
	
	public boolean isItOn(int id) {
		if(elevators.contains(id)){
			return true;
		}
		return false;
	}
}
