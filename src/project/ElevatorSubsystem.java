package project;

import java.io.IOException;
import java.net.*;
import java.util.*;


/**
 * 
 * @author Ryan, modified by Colton
 *
 */

public class ElevatorSubsystem {
	private DatagramSocket socket;
	private DatagramPacket receivePacket, sendPacket;
	private PacketHelper helper = new PacketHelper();
	private int elePort;
	private int schPort;
	private int floorPort;
	private int GUIPort;
	private int numEle;
	private int nextEle;
	private int nextFloor;
	private int destinationFloor;
	private int arrivedEle;
	private int arrivedFloor;
	private Map<Integer, String> eleList = new HashMap<>();
	
	//Server Socket on port 5000
	public ElevatorSubsystem(Config config) {
		//Import config File Properties
		numEle = config.getIntProperty("numEle");
		elePort = config.getIntProperty("elePort");
		schPort = config.getIntProperty("schPort");
		floorPort = config.getIntProperty("floorPort");
		GUIPort = config.getIntProperty("GUIPort");
		
		//Init
		nextEle = 0;
		nextFloor = 0;
		destinationFloor = 0;
		arrivedEle = 0;
		arrivedFloor = 0;
		
		try {
			socket = new DatagramSocket(elePort);
		} catch(SocketException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	//When a floor needs a elevator
	public void receivePacketOne() {
		System.out.println("Waiting for packet from Scheduler...");
        receivePacket = helper.receivePacket(socket);
        helper.print(receivePacket, "Elevator Subsystem", "received from Scheduler");
        
        System.out.println("\nContaining: " + new String(receivePacket.getData()) +"\n");
    }
	
	//Scheduler returning which elevator to send and to where
	synchronized void receivePacketTwo() {
        receivePacket = helper.receivePacket(socket);
        helper.print(receivePacket, "Elevator Subsystem", "received from Scheduler");
        
        System.out.println("\nContaining: " + new String(receivePacket.getData()) +"\n");
        
        String result = new String(receivePacket.getData());
        String[] data = result.split("\\|");
        nextEle = Integer.valueOf(data[0]);
        nextFloor = Integer.valueOf(data[1]);
        destinationFloor = Integer.valueOf(data[2]);
        notifyAll();
    }
	
	//Convert the message into a byte array then send it
	public void sendDataList() {
		byte[] data = formatDataList();
		sendPacket = helper.sendPacket(socket, data, schPort);
		helper.print(sendPacket, "Elevator Subsystem", "sent to Scheduler");
	}
	
	//Format a list to send to the scheduler
	public byte[] formatDataList() {
		String temp = "";
			
		for (Map.Entry mapElement : eleList.entrySet()) {
			int key = (int)mapElement.getKey();
			String value = (String)mapElement.getValue();
	            
			temp = temp + value + ";";
	    }
		
		byte[] data = new byte[temp.getBytes().length];
		
		//Add the message into the data byte array then set the byte after it to 0
		System.arraycopy(temp.getBytes(), 0, data, 0, temp.getBytes().length);
        
        return data;
	}
	
	public void sendDataListGUI() {
		byte[] data = formatDataList();
        sendPacket = helper.sendPacket(socket, data, GUIPort);
        helper.print(sendPacket, "Elevator Subsystem", "sent to GUI");
	}
	
	//Elevators use this to update the hashmap of elevator locations
	synchronized void updateData(int id, String info) {
        eleList.put(id, info);
        /*
        System.out.println("Info "+info);
        System.out.println("ID "+id);
        */
        
        notifyAll();
	}
	public Map<Integer, String> getEleList(){
		return this.eleList;
	}
	
	//Elevators use this to see if they have been sent to a floor
	synchronized int send(int id) {
		while(nextEle != id) {
			try{
				wait();
			} catch (InterruptedException e) {}
		}
        nextEle = 0;
        notifyAll();
        return nextFloor;
	}
	
	public int getDestinationFloor()
	{
		return destinationFloor;
	}
	
	public int getNumEle() {
		return numEle;
	}
	
	
	//Order that the methods run in
	public void run() {
		receivePacketOne();
		sendDataList();
		receivePacketTwo();
		sendDataListGUI();
	}

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
        Config config = new Config();
        ElevatorSubsystem eleSystem = new ElevatorSubsystem(config);
        System.out.println("Elevator System started");
        System.out.println("Number of Elevators: "+eleSystem.getNumEle());

        int incr = 1;
        for (int i = 0; i < eleSystem.getNumEle(); i++) {
            Thread tempThread = new Thread(new Elevator(i+1, incr, eleSystem), "Elevator: "+(i+1));
            tempThread.start();
            eleSystem.updateData(i+1, i+1+"|"+incr+"|0|0|0|0|0");
            incr += 6;
        }

        while(true) {
            eleSystem.run();
        }
    }

	public DatagramSocket getSocket() {
		return socket;
	}

	public DatagramPacket packetData() {
		return receivePacket;
	}
}
