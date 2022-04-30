/**
 * Test class for the Elevator 
 *
 * @author Iyamu Osaretinmwen
 */
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class ElevatorTest {
    private Thread elevator1;
    private Thread elevator2;
    private Thread elevator3;
    private Thread elevator4;
    private ElevatorSubsystem elevatorSubsystem;
    private Scheduler scheduler;
    private FloorSubsystem floorSubsystem;
    private Floor floor;
    private Config config;
    private ArrayList<String> data = new ArrayList<>();

    @BeforeEach
    void setUp() throws IOException {
        config = new Config();
        this.elevatorSubsystem = new ElevatorSubsystem(config);
        this.elevator1 = new Thread(new Elevator(1, 1, elevatorSubsystem));
        this.elevator2 = new Thread(new Elevator(2, 1, elevatorSubsystem));
        this.elevator3 = new Thread(new Elevator(3, 1, elevatorSubsystem));
        this.elevator4 = new Thread(new Elevator(4, 1, elevatorSubsystem));
        this.floorSubsystem = new FloorSubsystem(config);
        this.floor = new Floor(1, 4, floorSubsystem);
        scheduler = new Scheduler(config);

        this.data = new ArrayList<String>();
        data.add("Array of String");

    }

    @AfterEach
    void tearDown() {
        scheduler.sendReceiveSocket.close();
        floorSubsystem.sendReceiveSocket.close();
        elevatorSubsystem.getSocket().close();
    }

    @Test
    void run() {
        assertTrue(elevator1.isAlive() == false);
        assertTrue(elevator2.isAlive() == false);
        assertTrue(elevator3.isAlive() == false);
        assertTrue(elevator4.isAlive() == false);

        floorSubsystem.send("14:05:15.0 1 Up 4", floorSubsystem.getSchPort(), 1);
        elevator1.start();
        elevator2.start();
        elevator3.start();
        elevator4.start();

        assertTrue(elevator1.isAlive() == true);
        assertTrue(elevator2.isAlive() == true);
        assertTrue(elevator3.isAlive() == true);
        assertTrue(elevator4.isAlive() == true);


        //A method to test the timeout of the system.
        try{
            Thread.sleep(5000);
        }catch(Exception exception){}

        assertTrue(elevator1.isAlive() == false);
        assertTrue(elevator2.isAlive() == false);
        assertTrue(elevator3.isAlive() == false);
        assertTrue(elevator4.isAlive() == false);

    }
}