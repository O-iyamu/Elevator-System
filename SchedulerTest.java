/**
 * Test class for the Scheduler
 *
 * @author Iyamu Osaretinmwen
 */
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class SchedulerTest {
    Scheduler scheduler;
    FloorSubsystem floor;
    ElevatorSubsystem eleSub;
    Config config;

    @BeforeEach
    void setUp() throws IOException {
        config = new Config();
        scheduler = new Scheduler(config);
        floor = new FloorSubsystem(config);
        eleSub = new ElevatorSubsystem(config);
    }

    @AfterEach
    void tearDown() {
        scheduler.sendReceiveSocket.close();
        floor.sendReceiveSocket.close();
        eleSub.getSocket().close();
    }

    @Test
    void receivePacketFloorSubsystem() {
        //This method tests the scheduler's receive method. The floor sends packet to the scheduler and this method
        // confirms if the data is sent or not
        String data = "10000000 1 up 4";
        floor.send(data, 5000, 1);
        scheduler.receivePacketFloorSubsystem();
        int len = scheduler.getPacketData().getLength();
        String receivedData = new String(scheduler.getPacketData().getData(), 0, len);
        assertEquals(data, receivedData);
    }

    @Test
    void sendToElevatorSubsystem() {
        //The floor sends to the scheduler, the scheduler sends to the elevator subsystem and this method tests if the
        // packet is received by the elevator subsystem.
        String data = "14:05:15.0 1 Up 4";
        floor.send(data, 5000, 1);
        scheduler.receivePacketFloorSubsystem();
        eleSub.updateData(1, "1\\|0\\|0\\|0\\|0\\|1\\|0");
        scheduler.sendToElevatorSubsystem();

        eleSub.receivePacketOne();
        assertTrue(eleSub.packetData() != null);
    }


    @Test
    void sendToFloorSubsystem() {
        //The floor sends to the scheduler, the scheduler sends back to the floor subsystem and this method tests if the
        // packet is received by the floor subsystem.
        String data = "14:05:15.0 1 Up 4";
        floor.send(data, 5000, 1);
        scheduler.receivePacketFloorSubsystem();

        eleSub.updateData(1, "1|0|0|0|0|1|0");
        eleSub.updateData(2, "2|0|0|0|0|1|0");
        eleSub.updateData(3, "3|0|0|0|0|1|0");
        eleSub.updateData(4, "4|0|0|0|0|1|0");

        eleSub.sendDataList();

        scheduler.requestElevatorLocations();
        scheduler.sendToFloorSubsystem();

        floor.receive();
        int len = floor.packetData().getLength();
        System.out.println(floor.packetData().getData());
        String receivedData = new String(floor.packetData().getData(), 0, len);
        assertTrue(floor.packetData().getData() != null);
    }
}
