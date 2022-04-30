package project;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;


public class GUI extends JFrame{
	private DatagramSocket socket;
	private DatagramPacket receivePacket, sendPacket;
	private PacketHelper helper = new PacketHelper();
	private JPanel contentPane;
	private List<JTextPane> eleFloor = new ArrayList<JTextPane>();
	private List<JTextPane> eleText = new ArrayList<JTextPane>();
	private List<JTextPane> floorDirection = new ArrayList<JTextPane>();
	private List<JPanel> elePanel = new ArrayList<JPanel>();
	private List<JPanel> floorLamp = new ArrayList<JPanel>();
	private int GUIPort;
	private JTextPane scheduler;
	private Config config;

	/**
	 * Launch the application.
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException{
		GUI frame = new GUI();
		frame.setVisible(true);
		frame.setTitle("Elevator System - Group 8");
		
		while(true) {
			frame.receive();
			frame.repaint();
		}
	}

	/**
	 * Create the frame.
	 * @throws IOException 
	 */
	public GUI() throws IOException {
		config = new Config();
		
		GUIPort = config.getIntProperty("GUIPort");
		
		//Socket
		try {
			socket = new DatagramSocket(GUIPort);
		} catch(SocketException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		//INIT
		int width = config.getIntProperty("eleGUI") * (config.getIntProperty("numEle")) + 120;
		int height = config.getIntProperty("floorGUI") * config.getIntProperty("numFloors") + 300;
		int eleStart = (config.getIntProperty("floorGUI") * config.getIntProperty("numFloors")) - 15;
		int eleLocX = 150;
		int floorLocY = 0;
		
		//PANE INIT
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, width, height);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		//INIT Elevator Image at 180
		eleLocX = 180;
		for(int i = 0; i < config.getIntProperty("numEle"); i++) {
			elePanel.add(new JPanel());
			elePanel.get(i).setBounds(eleLocX + (config.getIntProperty("eleGUI") * i ), eleStart, 25, 25);
			elePanel.get(i).setBackground(Color.BLACK);
			contentPane.add(elePanel.get(i));
			
		}
		
		//INIT Floors at 0
		floorLocY = 0;
		for(int i = 0; i < config.getIntProperty("numFloors"); i++) {
			floorLamp.add(new JPanel());
			floorLamp.get(i).setBounds(5, floorLocY + (config.getIntProperty("floorGUI") * i ) + 15, 20, 20);
			floorLamp.get(i).setBackground(Color.WHITE);
			contentPane.add(floorLamp.get(i));
			
			floorDirection.add(new JTextPane());
			floorDirection.get(i).setBounds(30, floorLocY + (config.getIntProperty("floorGUI") * i )+ 15, 40, 20);
			floorDirection.get(i).setText("");
			floorDirection.get(i).setEditable(false);
			contentPane.add(floorDirection.get(i));
			
			JLabel lblNewLabel = new JLabel("Floor "+(config.getIntProperty("numFloors")-i));
			lblNewLabel.setBounds(75, floorLocY + (config.getIntProperty("floorGUI") * i ), 50, 50);
			contentPane.add(lblNewLabel);
		}
		
		//INIT Elevator Labels at 160
		eleLocX = 160;
		for(int i = 0; i < config.getIntProperty("numEle"); i++) {
			JLabel lblNewLabel_1 = new JLabel("Elevator "+(i+1));
			lblNewLabel_1.setBounds(eleLocX + (config.getIntProperty("eleGUI") * i ), config.getIntProperty("floorGUI") * config.getIntProperty("numFloors"), 80, 50);
			contentPane.add(lblNewLabel_1);
		}
		
		//INIT Elevator Text Panes at 100
		eleLocX = 100;
		for(int i = 0; i < config.getIntProperty("numEle"); i++) {
			eleText.add(new JTextPane());
			eleText.get(i).setBounds(eleLocX + (config.getIntProperty("eleGUI") * i ), (height - 220), 180, 60);
			eleText.get(i).setText("Elevator "+(i+1)+" is waiting...");
			eleText.get(i).setEditable(false);
			contentPane.add(eleText.get(i));
			
			eleFloor.add(new JTextPane());
			eleFloor.get(i).setBounds(eleLocX + (config.getIntProperty("eleGUI") * i ), (height - 250), 180, 20);
			eleFloor.get(i).setText("Floor 1");
			eleFloor.get(i).setEditable(false);
			contentPane.add(eleFloor.get(i));
		}
		
		//INIT Scheduler Text Pane
		scheduler = new JTextPane();
		scheduler.setBounds(100, height - 150, width-140, 80);
		scheduler.setText("Scheduler is waiting...");
		scheduler.setEditable(false);
		contentPane.add(scheduler);
	}
	
	public void eleSim(String data){
		System.out.println(data);
		String elevatorsStatus[] = data.trim().split(";");
		
		//Data
		int id;
		int destFloor;
		int direction;
		int curFloor;
		int state;
		
		int eleLocX;
		int eleLocY;
		
		for(String elevator : elevatorsStatus){
			System.out.println(elevator);
			elevator = elevator.replace(";", "");
		}
		
		for(String elevator : elevatorsStatus){
			String temp[] = elevator.trim().split("\\|");
			
			
			System.out.println("ID: "+temp[0]);
			System.out.println("DesFloor: "+temp[1]);
			System.out.println("Direction: "+temp[4]);
			System.out.println("CurFloor: "+temp[5]);
			System.out.println("State: "+temp[6]);
			
			
			id = Integer.parseInt(temp[0]);
			destFloor = Integer.parseInt(temp[1]);
			direction = Integer.parseInt(temp[4]);
			curFloor = Integer.parseInt(temp[5]);
			temp[6] = temp[6].replace(";", "");
			state = Integer.parseInt(temp[6]);
			
			eleLocX = 180;
			eleLocY = (config.getIntProperty("floorGUI") * (config.getIntProperty("numFloors") - (curFloor) + 1)) - 15;
			
			//Floors
			if(state != 0) { //Turn ON Floors DOESNT WORK
				floorLamp.get(config.getIntProperty("numFloors") - (destFloor + 3)).setBackground(Color.ORANGE);;
				if(direction==0) {
					floorDirection.get(config.getIntProperty("numFloors") - (destFloor)).setText("UP");
				}else if(direction==1){
					floorDirection.get(config.getIntProperty("numFloors") - (destFloor)).setText("DOWN");
				}
			}else { //Turn OFF Floors
				floorLamp.get(config.getIntProperty("numFloors") - (destFloor)).setBackground(Color.WHITE);;
				floorDirection.get(config.getIntProperty("numFloors") - (destFloor)).setText("");
			}
			
			//Elevator Current Floor
			eleFloor.get(id-1).setText("Floor " + curFloor);
			elePanel.get(id-1).setBounds(eleLocX + (config.getIntProperty("eleGUI") * (id - 1) ), eleLocY, 25, 25);
			
			if(state == 1) {
				eleText.get(id-1).setText("Moving to Pickup at Floor "+destFloor);
			}
			else if(state == 2) {
				eleText.get(id-1).setText("Opening Doors");
			}
			else if(state == 3) {
				eleText.get(id-1).setText("Passengers are getting on and picking a Destination");
			}
			else if(state == 4) {
				eleText.get(id-1).setText("Closing Doors");
			}
			else if(state == 5) {
				eleText.get(id-1).setText("Moving to Destination Floor "+destFloor);
			}
			else if(state == 6) {
				eleText.get(id-1).setText("Passengers are getting off");
			}
			else if(state == 7) {
				eleText.get(id-1).setText("Elevator Error");
			}
		}	
	}
	
	public void schSim(String data) {
		String temp[] = data.trim().split("\\|");
		
		int state = Integer.parseInt(temp[0]);
		String Message = temp[1];
	}
	
	//Recieve packet
	public void receive() {
        receivePacket = helper.receivePacket(socket);
        eleSim(new String(receivePacket.getData()));
    }
	
	/*
	public void updateEle(int id) {
		int eleNumber = id-1; //Floor index
		int chooseFloor = 4; //Floor to go to
		int actualFloor = config.getIntProperty("numFloors") - (chooseFloor); //Floor FOR CALCS
		int eleLocX = 180;
		int eleLocY = (config.getIntProperty("floorGUI") * (actualFloor+1)) - 15;
		
		floorLamp.get(actualFloor).setBackground(Color.ORANGE);
		floorDirection.get(actualFloor).setText("UP");
		eleFloor.get(eleNumber).setText("Floor " + chooseFloor);
		eleText.get(eleNumber).setText("Elevator is unloading");
		elePanel.get(eleNumber).setBounds(eleLocX + (config.getIntProperty("eleGUI") * eleNumber ), eleLocY, 25, 25);
	}
	*/
}
