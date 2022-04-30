package project;

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
 * Class Scheduler represents the scheduler of the elevator system.
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
	DatagramSocket sendReceiveSocket, receiveSocket;
	private int elePort;
	private int schPort;
	private int floorPort;
	private int GUIPort;
	
	public Scheduler(Config config){
		elePort = config.getIntProperty("elePort");
		schPort = config.getIntProperty("schPort");
		floorPort = config.getIntProperty("floorPort");
		GUIPort = config.getIntProperty("GUIPort");
		
		try {
			
			// Construct a datagram socket and bind it to port 5000 
			// on the local host machine. This socket will be used to
			// receive UDP Datagram packets.
			receiveSocket = new DatagramSocket(schPort);
			
			// Construct a datagram socket and bind it to any available 
			// port on the local host machine. This socket will be used to
			// send and receive UDP Datagram packets.
			sendReceiveSocket = new DatagramSocket();
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

	public enum SchedulerState
	{
		WaitingForButtonPress,
		RequestingElevatorLocations,
		SendingElevator,
		UpdateFloor;
	}
	static SchedulerState state = SchedulerState.WaitingForButtonPress;
	
	/**
	 * receive a packet from the floor subsystem.
	 */
	public void receivePacketFloorSubsystem()
	{
		// Construct a DatagramPacket for receiving packets up 
		// to 100 bytes long (the length of the byte array).
		byte data[] = new byte[100];
		receivePacket = new DatagramPacket(data, data.length);
		
		// Block until a datagram packet is received from receiveSocket.
		try {        
			System.out.println("Waiting for response from FloorSubsystem..."); // so we know we're waiting
			receiveSocket.receive(receivePacket);
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
	 * This function determines which elevator will be selected to pick up the passengers.
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
		
		System.out.println("Scheduler: Packet sent to ElevatorSubsystem to get elevator locations.\n");
		
		//Now we need to store the locations we get from the response
		// Construct a DatagramPacket for receiving packets up 
		// to 100 bytes long (the length of the byte array).
		byte info[] = new byte[100];
		serverPacket = new DatagramPacket(info, info.length);
		
		// Block until a datagram packet is received from receiveSocket.
		try {        
			System.out.println("Waiting for response from ElevatorSubsystem..."); // so we know we're waiting
			receiveSocket.receive(serverPacket);
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
		
		System.out.println("The id of the elevator to move is: "+id);
		elevatorToMove = Integer.toString(id);
	}
	
	/*
	 * Transfers the FloorSubsystem information over to the ElevatorSubsystem.
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
		
		System.out.println("Scheduler: Packet sent to ElevatorSubsystem.\n");
	}
	
	/*
	 * Sends the information from the server over to the client.
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
		
		System.out.println("Scheduler: Packet sent to FloorSubsystem.\n");
	}
	
    public static void main(String[] args) throws IOException{
    	Config config = new Config();
		Scheduler scheduler = new Scheduler(config);
		
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