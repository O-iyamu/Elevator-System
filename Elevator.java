
import java.util.ArrayList;
import java.io.IOException;


/**
 * @author Ryan, modified by Colton
 * 
 * Class Elevator represents a single elevator inside a building.
 *
 */
public class Elevator implements Runnable{
	private int id;
	private ElevatorSubsystem sysRef;
	private int pickupFloor = 0;
	private int destFloor = 0;
	private int curFloor = 0;
	private boolean hasPeople = false;
	private boolean boarding = true;
	private boolean errorDoor = false;
	private boolean errorFull = false;
	private Door door;
	private Motor motor;
	private ElevatorLamp lamp;
	private ElevatorButton button;
	Config config;
	
	/**
	 * Constructor for Elevator. Starts an Elevator thread. Assigns default values for id, floor, and 
	 * also creates door, motor, lamp, and button objects for this elevator.
	 * 
	 * @param id
	 * @param floor
	 * @param sysRef
	 */
	public Elevator(int id, int floor, ElevatorSubsystem sysRef) throws IOException {
		this.id=id;
		this.curFloor=floor;
		this.sysRef=sysRef;
		door = new Door(id, false, false);
		motor = new Motor(id, false, false);
		lamp = new ElevatorLamp(id, false);
		button = new ElevatorButton(id, false);
		config = new Config();
	}
	
	/**
	 * ElevatorState consists of four states that an elevator will transition through. All elevators initially start
	 * in the Waiting state.
	 *
	 */
	public enum ElevatorState
	{
		Waiting,
		Stopped,
		MovingToPassengers,
		MovingToDestination;
	}
	ElevatorState state = ElevatorState.Waiting;
	
	/**
	 * This is where elevators are constantly transitioning between states, corresponding to the information
	 * they receive once they have been sent to a location from the ElevatorSubsystem.
	 * 
	 */
	public void run()
	{
		while(true) {
			
			String temp = "";
			switch (state) {
			  
        /**
				 * This is the state upon which the elevator is waiting for further instructions and has either arrived at its
				 * destination, or is still waiting to be assigned a floor to go to.
				 */
				case Waiting:
					
					//Set ele as idle
					System.out.println("Elevator " +id+": is Idle");
					//System.out.println("Elevator "+id+" is currently waiting for further instruction...");
					temp = id+"|"+0+"|0|0|0|"+curFloor+"|0";//ID|FLOOR|PEOPLE|MOVING|DIRECTION|CURFLOOR|STATE
					sysRef.updateData(id, temp);
					
					//Find next destination
					pickupFloor = sysRef.send(id);
					destFloor = sysRef.getDestinationFloor();
					
					//Set ele to moving
					if(pickupFloor > curFloor) {//Going up
						temp = id+"|"+pickupFloor+"|0|0|0|"+curFloor+"|1";//ID|FLOOR|PEOPLE|MOVING|DIRECTION|CURFLOOR|STATE
					}
					else {//Going Down
						temp = id+"|"+pickupFloor+"|0|0|1|"+curFloor+"|1";//ID|FLOOR|PEOPLE|MOVING|DIRECTION
					}
					sysRef.updateData(id, temp); //update that elevator is moving
					
					System.out.println("Elevator "+id+": moving to floor "+pickupFloor);
					state = ElevatorState.MovingToPassengers;
					
					break;
				
        /**
				 * This is the state in which an elevator has just been given instructions, and is on its way to pick up passengers.
				 */
				case MovingToPassengers:
					
					//Move elevator floor by floor up or down
					System.out.println("Please wait until elevator "+id+" has arrived...");
					//int timing = 0;
					
					//going up
          //While we are going up, increment the current floor up by 1, and update the ElevatorSubsystem that we are at a new floor.
					while(pickupFloor > curFloor){ 
						
						motor.elevatorGoingUp();
						try {
							Thread.sleep((long) config.getFloatProperty("timePerFloor"));
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						//timing = destFloor - curFloor;
						curFloor++;
						temp = id+"|"+pickupFloor+"|0|0|0|"+curFloor+"|1";//ID|FLOOR|PEOPLE|MOVING|DIRECTION|CURFLOOR|STATE
						sysRef.updateData(id, temp);
					}
					
					//going down
          //While we are going down, increment the current floor down by 1, and update the ElevatorSubsystem that we are at a new floor.
					while(pickupFloor < curFloor) {
						
						motor.elevatorGoingDown();
						try {
							Thread.sleep((long) config.getFloatProperty("timePerFloor"));
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						//timing = floor - destFloor;
						curFloor--;
						temp = id+"|"+pickupFloor+"|0|0|0|"+curFloor+"|1";//ID|FLOOR|PEOPLE|MOVING|DIRECTION|CURFLOOR|STATE
						sysRef.updateData(id, temp);
					}
					
					System.out.println("Elevator " +id+": has arrived at floor "+pickupFloor+".");
					motor.elevatorArrived();
					state = ElevatorState.Stopped;
					
					break;
					
         /**
				 * This is the state in which the elevator has either arrived to pick passengers up, or to drop them off.
				 * When we are picking up passengers, we go to the MovingToDestination state. When we are dropping them off,
				 * we go back to the waiting state after they disembark.
				 */
				case Stopped:
					
					int people = 0;
					
					//Opening Doors
					temp = id+"|"+curFloor+"|"+people+"|0|0|"+curFloor+"|2";//ID|FLOOR|PEOPLE|MOVING|DIRECTION
					sysRef.updateData(id, temp); //Update that elevator has stopped
					try {
						Thread.sleep((long) config.getFloatProperty("timeDoor"));
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					door.open();
					
					//Sleep while customers are getting on/off, then switch to waiting.
					if(boarding){
						hasPeople = true;
						people = 1;
						System.out.println("\nElevator "+id+": stopped, people are boarding.\n");
						boarding = false;
						button.elevatorButtonPressed();
						lamp.turnElevatorLampOn();
					}
					else {
						people = 0;
						hasPeople = false;
						System.out.println("\nElevator "+id+": stopped, people are disembarking.\n");
						boarding = true;
					}
					
					// set ele as people getting on or off then sleep
					if(hasPeople) {//Getting on
						temp = id+"|"+curFloor+"|"+people+"|0|0|"+curFloor+"|3";//ID|FLOOR|PEOPLE|MOVING|DIRECTION|STATE
						sysRef.updateData(id, temp); //Update that elevator has stopped
					}
					else {//Getting off
						temp = id+"|"+curFloor+"|"+people+"|0|0|"+curFloor+"|6";//ID|FLOOR|PEOPLE|MOVING|DIRECTION|STATE
						sysRef.updateData(id, temp); //Update that elevator has stopped
					}
					try {
						Thread.sleep((long) config.getFloatProperty("timeBoard"));
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					//Closing Doors
					temp = id+"|"+curFloor+"|"+people+"|0|0|"+curFloor+"|4";//ID|FLOOR|PEOPLE|MOVING|DIRECTION|STATE
					sysRef.updateData(id, temp); //Update that elevator has stopped
					try {
						Thread.sleep((long) config.getFloatProperty("timeDoor"));
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					door.close();
					
					if(!boarding) {
						state = ElevatorState.MovingToDestination;
					}
					else {
						state = ElevatorState.Waiting;
					}
					
					break;
					
        /**
				 * This is the state in which passengers have boarded the elevator, and are now being taken to their
				 * destination floor.
				 */
				case MovingToDestination:
					
					//move ele to destination one floor at a time up or down
					System.out.println("Elevator "+id+": Bringing passengers to floor "+destFloor+".");
					door.close();
					//timing = 0;
          //While we are going up, increment the current floor up by 1, and update the ElevatorSubsystem that we are at a new floor.
					while(destFloor > curFloor){//going up
						//timing = destination - destFloor;
						motor.elevatorGoingUp();
						try {
							Thread.sleep((long) config.getFloatProperty("timePerFloor"));
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						curFloor++;
						temp = id+"|"+destFloor+"|1|0|0|"+curFloor+"|5";//ID|FLOOR|PEOPLE|MOVING|DIRECTION|CURFLOOR|STATE
						sysRef.updateData(id, temp);
						
					}
          //While we are going down, increment the current floor down by 1, and update the ElevatorSubsystem that we are at a new floor.
					while(destFloor < curFloor){//going down
						//timing = destFloor - curFloor;
						motor.elevatorGoingDown();
						try {
							Thread.sleep((long) config.getFloatProperty("timePerFloor"));
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						curFloor--;
						temp = id+"|"+destFloor+"|1|0|0|"+curFloor+"|5";//ID|FLOOR|PEOPLE|MOVING|DIRECTION|CURFLOOR|STATE
						sysRef.updateData(id, temp);
					}
					
					//Arrived so update ele
					System.out.println("Elevator " +id+": arrived at floor "+destFloor+".");
					curFloor = destFloor;
					temp = id+"|"+destFloor+"|"+"0|0|0|"+curFloor+"|5";
					sysRef.updateData(id, temp);
					motor.elevatorArrived();
					state = ElevatorState.Stopped;
					break;
			}
		}
	}
}