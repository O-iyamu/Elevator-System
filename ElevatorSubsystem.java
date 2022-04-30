
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
	
	/**
	 * Constructor for ElevatorSubsystem. Sets the socket on port 5000 and defaults
	 * 
	 * @param config	which is a reference to the Config class
	 */
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
	
	/**
	 * Waits for a request for the list of elevators from the scheduler
	 * 
	 */
	public void receivePacketOne() {
		System.out.println("Waiting for packet from Scheduler...");
        receivePacket = helper.receivePacket(socket);
        helper.print(receivePacket, "Elevator Subsystem", "received from Scheduler");
        
        System.out.println("\nContaining: " + new String(receivePacket.getData()) +"\n");
    }
	
	/**
	 * Waits for the scheduler to tell it which elevator to send to which floor
	 * 
	 */
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
	
	/**
	 * Sends the list of elevators to the scheduler using a packet and formatDataList() 
	 * 
	 */
	public void sendDataList() {
		byte[] data = formatDataList();
		sendPacket = helper.sendPacket(socket, data, schPort);
		helper.print(sendPacket, "Elevator Subsystem", "sent to Scheduler");
	}
	
	//Format a list to send to the scheduler
	/**
	 * Converts the list of elevators into a byte array format to use in a packet
	 * 
	 * @return data		byte[] made from eleList
	 */
	private byte[] formatDataList() {
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
	
	/**
	 * Sends the list of elevators to the GUI using a packet and formatDataList()
	 * 
	 */
	public void sendDataListGUI() {
		byte[] data = formatDataList();
        sendPacket = helper.sendPacket(socket, data, GUIPort);
        //helper.print(sendPacket, "Elevator Subsystem", "sent to GUI");
	}
	
	/**
	 * Elevators use this to update the hashmap eleList that is full of elevator locations
	 * 
	 */
	synchronized void updateData(int id, String info) {
        eleList.put(id, info);
        sendDataListGUI();
        
        notifyAll();
	}
	
	/**
	 * Elevators use this to check if they have been sent to a floor
	 * 
	 * @return nextFloor	represents the floor they are sent to
	 */
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
	
	/**
	 * Simple getters
	 * 
	 */
	public int getDestinationFloor(){return destinationFloor;}
	
	public int getNumEle() {return numEle;}
	
	public DatagramSocket getSocket() {return socket;}

	public DatagramPacket packetData() {return receivePacket;}

	public Map<Integer, String> getEleList() {return eleList;}
	
	
	/**
	 * Order that the methods run in
	 * 
	 */
	public void run() {
		receivePacketOne();
		sendDataList();
		receivePacketTwo();
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

        for (int i = 0; i < eleSystem.getNumEle(); i++) {
            Thread tempThread = new Thread(new Elevator(i+1, 1, eleSystem), "Elevator: "+(i+1));
            tempThread.start();
            eleSystem.updateData(i+1, i+1+"|"+0+"|0|0|0|1|0");
        }

        while(true) {
            eleSystem.run();
        }
    }
}
