package project;

import org.testng.annotations.Test;
import org.junit.jupiter.api.BeforeEach;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import static org.junit.jupiter.api.Assertions.*;

class GUITest {
    private Thread floor;
    private Thread elevator;


    private Scheduler scheduler;
    private Config config;
    private FloorSubsystem floorSubsystem;
    private ElevatorSubsystem elevatorSubsystem;


    @BeforeEach
    void setUp() throws IOException {
        config = new Config();
        floorSubsystem = new FloorSubsystem(config);
        floor = new Thread(new Floor(1, 4, floorSubsystem));
        elevator = new Thread(new Elevator(1, 1, elevatorSubsystem));
        scheduler = new Scheduler(config);

    }

    @Test
    public void testFailCase() {

       //To write into the system

        //Start the threads

        // Wait for 5 seconds
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.exit(1);
        }

        assert !floor.isAlive();
        assert !elevator.isAlive();
    }
}