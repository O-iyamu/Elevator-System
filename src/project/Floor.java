package project;

import java.util.*;

/**
 * @author Colton, Ryan
 * 
 * Class Floor represents a single floor in a building.
 *
 */
public class Floor implements Runnable{
	private int id;
	private boolean top,bottom,isRequesting;
	private FloorSubsystem sysRef;
	private FloorLamp lamp= new FloorLamp();;
	private FloorButton button = new FloorButton();
	private List<Integer> elevators = new ArrayList<>();
	
	/**
	 * Floor Constructor
	 */
	public Floor(int id, int numEle, FloorSubsystem sysRef){
		this.id = id;
		this.sysRef = sysRef;
		isRequesting = false;
		
		if(this.id == 1) {
			bottom = true;
		}else {
			bottom = false;
		}
		
		if(this.id == numEle ) {
			top = true;
		}else {
			top = false;
		}
    }
	
	public void run(){
		while(true) {
			elevators = sysRef.eleComing(id);
			
			lamp.update(elevators);
			
			isRequesting = sysRef.isRequesting(id);
			
			if(isRequesting && !button.getIsOn()) {
				
				if(top && sysRef.isRequestingUp(id)) {
					System.out.println("There is no up button");
				}else if(bottom && !sysRef.isRequestingUp(id)) {
					System.out.println("There is no down button");
				}else {
					button.turnOn(sysRef.isRequestingUp(id));
				}
			}else {
				button.turnOff();
			}
		}
	}
}