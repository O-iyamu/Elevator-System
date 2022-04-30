
import java.io.*;
import java.net.*;

/**
 * @author Ryan
 *
 */
public class PacketHelper {
	
	private DatagramPacket receivePacket,sendPacket;
	private static final int BUFF = 100;
	
	//Receive a packet
	public DatagramPacket receivePacket(DatagramSocket socket) {
		byte data[] = new byte[BUFF];
		receivePacket = new DatagramPacket(data, data.length);
		
		try {
			socket.receive(receivePacket);
		}catch(IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
		
		return receivePacket;
	}
	
	//Send a Packet
	public DatagramPacket sendPacket(DatagramSocket socket, byte[] data, int port) {
		try {
			sendPacket = new DatagramPacket(data, data.length, InetAddress.getLocalHost(), port);
		}catch (UnknownHostException e) {
	         e.printStackTrace();
	         System.exit(1);
	      }
		
		try {
            socket.send(sendPacket);
        } catch(IOException e) {
        	e.printStackTrace();
            System.exit(1);
         }
		
		return sendPacket;
	}
	
	//Print a Packet
	public void print(DatagramPacket packet, String host, String type) {
		byte data[] = packet.getData();
		int counter = 0;
		
		System.out.println(host+ ":\n\tPacket " +type+ ":");
        System.out.println("\tFrom host: " + packet.getAddress() +":"+  packet.getPort());
       
        if(data[1] == (byte)0 || data[1] == (byte)1 || data[1] == (byte)2) {
        	
        	System.out.println("\tLength: "    + packet.getData().length);
        	System.out.print("\tContaining [byte]: ");
            for(byte n : packet.getData()) {
                System.out.print(n+ " ");
                
                if(n == (byte)0) {
                	counter++;
                }
                
                if(data[1] == (byte)0 && counter == 4) {
                	break;
                }else if(!(data[1] == (byte)0) && counter == 3){
                	break;
                }
            }
        }else {
        	
        	System.out.println("\tLength: 4");
        	System.out.print("\tContaining [byte]: ");
            for(int i = 0; i<4; i++) {
                System.out.print(data[i]+ " ");
            }
        }

        System.out.println("\n\tContaining: " + new String(packet.getData()) +"\n\n");
	}
}
