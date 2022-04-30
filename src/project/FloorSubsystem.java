package project;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



/**
 * @author Colton, Ryan
 * 
 * Class Floor represents a single floor in a building.
 *
 */
public class FloorSubsystem{
	private static ArrayList<String> data = new ArrayList<String>();
	private Map<Integer, Boolean> isRequesting = new HashMap<>();
	private Map<Integer, List<Integer>> activeList = new HashMap<>();
	
	DatagramPacket sendPacket, receivePacket;
	DatagramSocket sendReceiveSocket;
	private int elePort;
	private int schPort;
	private int floorPort;
	private int GUIPort;
	private int numFloors;
	private String inputFileName;
	
	/**
	 * Floor Constructor
	 */
	public FloorSubsystem(Config config){
		//Import config File Properties
		numFloors = config.getIntProperty("numFloors");
		inputFileName = config.getStrProperty("inputFileName");
		elePort = config.getIntProperty("elePort");
		schPort = config.getIntProperty("schPort");
		floorPort = config.getIntProperty("floorPort");
		GUIPort = config.getIntProperty("GUIPort");
		
       try {
          // Construct a datagram socket and bind it to any available 
          // port on the local host machine. This socket will be used to
          // send and receive UDP Datagram packets.
          sendReceiveSocket = new DatagramSocket(floorPort);
       } catch (SocketException se) {   // Can't create the socket.
          se.printStackTrace();
          System.exit(1);
       }
       data = read();
    }
	
	/**
    * Sends a message to the Scheduler with port number 5000.
    * 
    * 
    * @param data
    * @param port
    */
   public void send(String data, int port, int type){
	   String temp[] = data.trim().split(" ");
	   if(temp[2].equalsIgnoreCase("up")) {
		   isRequesting.put(Integer.parseInt(temp[1]),true);
	   }else {
		   isRequesting.put(Integer.parseInt(temp[1]),false);
	   }
	   
	   //Find length of message
	   int length = data.getBytes().length;
	   
	   //Create byte array and assign first byte to 0.
	   byte[] toSend = new byte[length];
	   
	   //now we need to insert the message from data into our byte array.
	   System.arraycopy(data.getBytes(), 0, toSend, 0, data.getBytes().length);
	   
	   //Prepare the packet
	   try {
	         sendPacket = new DatagramPacket(toSend, toSend.length,
	                                         InetAddress.getLocalHost(), port);
	      } catch (UnknownHostException e) {
	         e.printStackTrace();
	         System.exit(1);
	      } 
	   
	   System.out.println("FloorSubsystem: Sending packet:");
       System.out.println("To Scheduler: " + sendPacket.getAddress());
       System.out.println("Destination Scheduler port: " + sendPacket.getPort());
       int len = sendPacket.getLength();
       System.out.println("Length: " + len);
       System.out.print("Containing: ");
       System.out.println(new String(sendPacket.getData(),0,len)); // or could print "s" 

       // Send the datagram packet to the server via the send/receive socket. 
       try {
          sendReceiveSocket.send(sendPacket);
       } catch (IOException e) {
          e.printStackTrace();
          System.exit(1);
       }
       System.out.println("FloorSubsystem: Packet sent.\n");
   }
   
   public void receive()
   {
	   // Construct a DatagramPacket for receiving packets up 
       // to 100 bytes long (the length of the byte array).
       byte data[] = new byte[100];
       receivePacket = new DatagramPacket(data, data.length);
       
       System.out.println("\nWaiting for response...");
       try {
          // Block until a datagram is received via sendReceiveSocket.  
          sendReceiveSocket.receive(receivePacket);
       } catch(IOException e) {
          e.printStackTrace();
          System.exit(1);
       }

       // Process the received datagram.
       System.out.println("FloorSubsystem: Packet received:");
       System.out.println("From Scheduler: " + receivePacket.getAddress());
       System.out.println("Scheduler port: " + receivePacket.getPort());
       int len = receivePacket.getLength();
       System.out.println("Length: " + len);
       System.out.print("Containing: "); 
       System.out.println(new String(receivePacket.getData()));
       System.out.println("\n");
       
       String info = new String(receivePacket.getData());
       String temp[] = info.trim().split("\\|"); //INfo on which elevator is sent where FloorNUM|EleNUm
       
       int type = Integer.parseInt(temp[2]);
       int ele = Integer.parseInt(temp[1]);
       int floor = Integer.parseInt(temp[0]);
       
       //Type 1 is elevator coming Type 2 is elevator done
       if(type == 1) { //TYPE 1
	       if(activeList.containsKey(floor)) {
	    	   if(!activeList.get(floor).contains(ele)) {
	    		   activeList.get(floor).add(ele);
	    	   }
	       }else {
	    	   List<Integer> elevators = new ArrayList<>();
	    	   elevators.add(ele);
	    	   activeList.put(floor,elevators);
	       }
       }else { // TYPE 2
    	   if(activeList.containsKey(floor)) {
	    	   if(activeList.get(floor).contains(ele)) {
	    		   activeList.get(floor).remove(ele);
	    	   }
	       }
       }
       
   }
   
   synchronized boolean isRequesting(int id) {
	   if(isRequesting.containsKey(id)) {
		   return true;
	   }
	   return false;
   }
   
   synchronized boolean isRequestingUp(int id) {
	   while(!isRequesting.containsKey(id)) {
			try{
				wait(50000);
			} catch (InterruptedException e) {}
		}
       
       notifyAll();
	   return isRequesting.get(id);
   }
   
   //Floors use this to see if an elevator is coming
   synchronized List<Integer> eleComing(int id) {
		while(!activeList.containsKey(id)) {
			try{
				wait(50000);
			} catch (InterruptedException e) {}
		}
        
        notifyAll();
        return activeList.get(id);
	}
	
	/**
	 * Reads input from a text file, and stores it in an ArrayList
	 * of type String.
	 * 
	 * @return data
	 */
	public ArrayList<String> read(){
		ArrayList<String> data = new ArrayList<String>();
		
		try {
	        File file = new File(inputFileName);
	
	        Scanner input = new Scanner(file);
	
	        while (input.hasNextLine()) {
	            String line = input.nextLine();
	            data.add(line);
	            //System.out.println(line);
	        }
	        input.close();
	
	    } catch (Exception ex) {
	        ex.printStackTrace();
	    }
		return data;
	}
	
	public DatagramPacket packetData() {
		return receivePacket;
	}

	public int getSchPort() {
	   return schPort;
   }
   public int getElePort() {
	   return elePort;
   }
   public int getGUIPort() {
	   return GUIPort;
   }
   public int getFloorPort() {
	   return floorPort;
   }
	
   public static void main(String args[]) throws IOException{
       Config config = new Config();
       FloorSubsystem c = new FloorSubsystem(config);

       long startTime = System.currentTimeMillis();

       for(String d: data){
           c.send(d, c.getSchPort(), 1);
           c.receive();
           try {
               Thread.sleep(3000);
           } catch (InterruptedException e) {
               e.printStackTrace();
           }
       }
       System.out.println("Time elapsed: "+(System.currentTimeMillis() - startTime)+ "ms");
   }
}