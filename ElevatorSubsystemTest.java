/**
 * Test class for the Elevator Subsystem
 *
 * @author Iyamu Osaretinmwen
 */

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class ElevatorSubsystemTest {
    private ElevatorSubsystem elevSub;
    private Scheduler scheduler;
    private FloorSubsystem floor;
    Config config;

    @BeforeEach
    void setUp() throws IOException {
        config = new Config();
        elevSub = new ElevatorSubsystem(config);
        scheduler = new Scheduler(config);
        floor = new FloorSubsystem(config);
    }

    @AfterEach
    void tearDown() {
        //Close all sockets after each call
        scheduler.sendReceiveSocket.close();
        floor.sendReceiveSocket.close();
        elevSub.getSocket().close();
    }

    @Test
    void receivePacketOne() {
        //To test this method, data is sent to the scheduler and then to the elevator subsystem.
        // This test ensures that the data is received
        floor.send("data 1 for 4", 5000, 1);
        scheduler.receivePacketFloorSubsystem();
        scheduler.sendToElevatorSubsystem();
        elevSub.receivePacketOne();
        assertTrue(elevSub.packetData() != null);
    }


    @Test
    void sendDataList() {
        //This method tests that the data sent to the scheduler is reflected in the packet of the scheduler.
        elevSub.updateData(1, "1|0|0|0|0|1|0");
        elevSub.updateData(2, "2|0|0|0|0|1|0");
        elevSub.updateData(3, "3|0|0|0|0|1|0");
        elevSub.updateData(4, "4|0|0|0|0|1|0");
        elevSub.sendDataList();
        scheduler.requestElevatorLocations();
        assertTrue(scheduler.getServerPacket() != null);
    }

    @Test
    void UpdateData() {
        //This method tests that the data is stored in the map
        elevSub.updateData(1, "1|0|0|0|0|1|0");
        assertTrue(elevSub.getEleList().containsKey(1));
    }

}