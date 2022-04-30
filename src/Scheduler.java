
import javax.xml.crypto.Data;
import java.awt.Taskbar.State;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;


/**
 * @author Colton
 * 
 * Class Scheduler represents the scheduler of the elevator system. The Scheduler receives a request from
 * the FloorSubsystem which contains information about the timestamp, request floor, direction, and destination floor.
 * The Scheduler then requests information from the ElevatorSubsystem as to where all of the elevators currently are. 
 * The Scheduler then decides which elevator should pick up the passengers, and sends information back to the ElevatorSubsystem
 * as to what elevator is going, where the passengers are, and where they want to be taken. The scheduler then waits for more 
 * requests from the FloorSubsystem.
 *
 */
public class Scheduler{
	private ArrayList<String> data;
	String timestamp;
	int requestFloor;
	String direction;
	int floorDestination;
	int stuckElevator;
	int stuckDoor;
	
	String[] elevatorsStatus = new String[4];
	String elevatorToMove;

	DatagramPacket sendPacket, receivePacket, serverPacket;
	DatagramSocket sendReceiveSocket;
	private int elePort;
	private int schPort;
	private int floorPort;
	private int GUIPort;
	
	/**
	 * Constructor for the Scheduler class. Gathers information from the config file.
	 * 
	 * @param config
	 */
	public Scheduler(Config config){
		elePort = config.getIntProperty("elePort");
		schPort = config.getIntProperty("schPort");
		floorPort = config.getIntProperty("floorPort");
		GUIPort = config.getIntProperty("GUIPort");
		
		try {
			
			// Construct a datagram socket and bind it to any available 
			// port on the local host machine. This socket will be used to
			// send and receive UDP Datagram packets.
			sendReceiveSocket = new DatagramSocket(schPort);
		} catch (SocketException se) {   // Can't create the socket.
			se.printStackTrace();
			System.exit(1);
		}
	}

	public DatagramPacket getPacketData() {
		return receivePacket;
	}

	public DatagramPacket getServerPacket() {
		return serverPacket;
	}

	/**
	 * SchedulerStates are the states which the Scheduler traverses through.
	 * The scheduler starts in the WaitingForButtonPress state.
	 *
	 */
	public enum SchedulerState
	{
		WaitingForButtonPress,
		RequestingElevatorLocations,
		SendingElevator,
		UpdateFloor;
	}
	static SchedulerState state = SchedulerState.WaitingForButtonPress;
	
	/**
	 * Receives a packet from the floor subsystem. Parses the data into variables for the timestamp,
	 * request floor, direction and destination floor.
	 */
	public void receivePacketFloorSubsystem()
	{
		// Construct a DatagramPacket for receiving packets up 
		// to 100 bytes long (the length of the byte array).
		byte data[] = new byte[100];
		receivePacket = new DatagramPacket(data, data.length);
		
		// Block until a datagram packet is received from receiveSocket.
		try {    
			sendToGUI("Waiting for response from FloorSubsystem.");
			System.out.println("Waiting for response from FloorSubsystem..."); // so we know we're waiting
			sendReceiveSocket.receive(receivePacket);
		} catch (IOException e) {
			System.out.print("IO Exception: likely:");
			System.out.println("Receive Socket Timed Out.\n" + e);
			e.printStackTrace();
			System.exit(1);
		}
		
		// Process the received datagram.
		System.out.println("Scheduler: Packet received:");
		System.out.println("From FloorSubsystem: " + receivePacket.getAddress());
		System.out.println("FloorSubsystem port: " + receivePacket.getPort());
		int len = receivePacket.getLength();
		System.out.println("Length: " + len);
		System.out.print("Containing: "); 
		
		// Form a String from the byte array.
		String received = new String(data,0,len);   
		
		
		String[] temp = received.split(" ");
		timestamp = temp[0];
		System.out.println("timestamp: "+ timestamp);
		requestFloor = Integer.parseInt(temp[1]);
		System.out.println("request floor: "+ requestFloor);
		direction = temp[2];
		System.out.println("direction: "+ direction);
		floorDestination = Integer.parseInt(temp[3]);
		System.out.println("floorDestination: "+ floorDestination);
	}
	
	/*
	 * This function is used to request the locations of all the elevators, and then determine which
	 * elevator should pick up the passengers.
	 */
	public void requestElevatorLocations()
	{	
		byte data[] = new byte[100];
		data[0] = 1;
		//prepare a packet to send to server for a request to find the location of the elevators
		try {
			sendPacket = new DatagramPacket(data, data.length, InetAddress.getLocalHost(), elePort);
		} catch (UnknownHostException e) {
	         e.printStackTrace();
	         System.exit(1);
	      }
		//Send the packet
		try {
			sendReceiveSocket.send(sendPacket);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		sendToGUI("Packet sent to ElevatorSubsystem to get elevator locations.");
		System.out.println("Scheduler: Packet sent to ElevatorSubsystem to get elevator locations.\n");
		
		//Now we need to store the locations we get from the response
		// Construct a DatagramPacket for receiving packets up 
		// to 100 bytes long (the length of the byte array).
		byte info[] = new byte[100];
		serverPacket = new DatagramPacket(info, info.length);
		
		// Block until a datagram packet is received from receiveSocket.
		try {        
			System.out.println("Waiting for response from ElevatorSubsystem..."); // so we know we're waiting
			sendReceiveSocket.receive(serverPacket);
		} catch (IOException e) {
			System.out.print("IO Exception: likely:");
			System.out.println("Receive Socket Timed Out.\n" + e);
			e.printStackTrace();
			System.exit(1);
		}
		
		//Process the received datagram and determine which elevator will go to the floor.
		System.out.println("Scheduler: Elevator locations received from ElevatorSubsystem:");
		System.out.println("From ElevatorSubsystem: " + serverPacket.getAddress());
		System.out.println("ElevatorSubsystem port: " + serverPacket.getPort());
		int len = serverPacket.getLength();
		System.out.println("Length: " + len);
		System.out.print("Containing: "); 
		System.out.println(new String(serverPacket.getData()));
		
		//parsing the result from elevator subsystem...
		String result = new String(serverPacket.getData());
		elevatorsStatus = result.split(";");
		
		//Choosing which elevator to send to pick up passengers.
		int i = 0;
		int num = 0;
		int distance = 99999;
		int id = 1;
		int allElevatorsBusy = 0;
		for(String elevator : elevatorsStatus)
		{
			String[] temp = elevator.split("\\|");
			num = Integer.parseInt(temp[1]);
			
			if(Integer.parseInt(temp[0]) == stuckElevator)
			{
				i++;
				allElevatorsBusy++;
				System.out.println("Elevator "+stuckElevator+" is stuck.");
				continue;
			}
			
			if(i == 0)
			{
				distance = Math.abs(num - requestFloor);
			}
				
			if(temp[2].equals("0"))
			{
				int iDistance = Math.abs(num - requestFloor);
				if(iDistance < distance)
				{
					id = i+1;
					distance = iDistance;
				}
			}
			else
			{
				allElevatorsBusy++;
			}
			
			i++;
			if(i == 4)
			{
				break;
			}
		}
		
		//Seeing if all elevators have passengers.
		if(allElevatorsBusy == 4)
		{
			System.out.println("All elevators are currently moving");
			id = 1;
			distance = 99999;
			for(String elevator : elevatorsStatus)
			{
				String[] temp = elevator.split("\\|");
				num = Integer.parseInt(temp[1]);
				
				if(Integer.parseInt(temp[0]) == stuckElevator)
				{
					i++;
					allElevatorsBusy++;
					System.out.println("Elevator "+stuckElevator+" is stuck.");
					continue;
				}
				int iDistance = Math.abs(num - requestFloor);
				if(iDistance < distance)
				{
					id = i+1;
					distance = iDistance;
				}
				i++;
				if(i == 4)
				{
					break;
				}
			}
		}
		
		allElevatorsBusy = 0;
		sendToGUI("The id of the elevator to move is: "+id);
		System.out.println("The id of the elevator to move is: "+id);
		elevatorToMove = Integer.toString(id);
	}
	
	/*
	 * This function sends information to the ElevatorSubsystem as to what elevator is to move, where it is picking up passengers,
	 * and where it is taking them. 
	 */
	public void sendToElevatorSubsystem()
	{
		 //Create byte array and assign first byte to 0.
		 byte[] toSend = new byte[100];
		 
		 String format = elevatorToMove+"|"+requestFloor+"|"+floorDestination+"|";
		 //now we need to insert the message from data into our byte array.
		 System.arraycopy(format.getBytes(), 0, toSend, 0, format.getBytes().length);
		
		//prepare the packet to send to server
		try {
			sendPacket = new DatagramPacket(toSend, toSend.length, InetAddress.getLocalHost(), elePort);
		} catch (UnknownHostException e) {
	         e.printStackTrace();
	         System.exit(1);
	      }
		
		//Send the packet
		try {
			sendReceiveSocket.send(sendPacket);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		sendToGUI("Packet sent to ElevatorSubsystem.");
		System.out.println("Scheduler: Packet sent to ElevatorSubsystem.\n");
	}
	
	/*
	 * This function is used for sending data back to the FloorSubsystem as in what elevator is coming, and where it is going to take them.
	 */
	public void sendToFloorSubsystem()
	{
		String floorInfo= floorDestination+"|"+elevatorToMove+"|1";
		
		//Create byte array and assign first byte to 0.
		 byte[] toSend = new byte[100];
		   
		 //now we need to insert the message from data into our byte array.
		 System.arraycopy(floorInfo.getBytes(), 0, toSend, 0, floorInfo.getBytes().length);
		
		//prepare the packet to send to client
		try {
			sendPacket = new DatagramPacket(toSend, toSend.length, InetAddress.getLocalHost(), receivePacket.getPort());
		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.exit(1);
		}
		//Send to client
		try {
			sendReceiveSocket.send(sendPacket);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		sendToGUI("Packet sent to FloorSubsystem.");
		
		System.out.println("Scheduler: Packet sent to FloorSubsystem.\n");
	}
	
	/**
	 * Used for sending information over to the GUI, which it will then display in the Scheduler text field.
	 * 
	 * @param message
	 */
	public void sendToGUI(String message){
		String GUIInfo= 0+"|"+message;
		
		//Create byte array and assign first byte to 0.
		 byte[] toSend = new byte[100];
		   
		 //now we need to insert the message from data into our byte array.
		 System.arraycopy(GUIInfo.getBytes(), 0, toSend, 0, GUIInfo.getBytes().length);
		
		//prepare the packet to send to client
		try {
			sendPacket = new DatagramPacket(toSend, toSend.length, InetAddress.getLocalHost(), GUIPort);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.exit(1);
		}
		//Send to client
		try {
			sendReceiveSocket.send(sendPacket);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
    public static void main(String[] args) throws IOException{
    	Config config = new Config();
		Scheduler scheduler = new Scheduler(config);
		
		//Constantly traverses through the following states. 
		while(true)
		{
			switch (state)
			{
				case WaitingForButtonPress:
					scheduler.receivePacketFloorSubsystem();
					state = SchedulerState.RequestingElevatorLocations;
					break;
					
				case RequestingElevatorLocations:
					scheduler.requestElevatorLocations();
					state = SchedulerState.SendingElevator;
					break;
					
				case SendingElevator:
					scheduler.sendToElevatorSubsystem();
					state = SchedulerState.UpdateFloor;
					break;
					
				case UpdateFloor:
					scheduler.sendToFloorSubsystem();
					state = SchedulerState.WaitingForButtonPress;
					break;
			}

		}
		
	}
    
}