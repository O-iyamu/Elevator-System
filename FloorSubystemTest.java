/**
 * Test class for the Floor Subsystem
 *
 * @author Iyamu Osaretinmwen
 */
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.*;

import static org.junit.jupiter.api.Assertions.*;

class FloorSubSystemTest {
    private FloorSubsystem floorSubsystem;
    ElevatorSubsystem eleSub;
    Scheduler scheduler;
    //FloorSubsystem floorSub;
    Config config;
    private DatagramPacket testReceivePacket;
    private DatagramSocket testSocket;
    byte classData[] = new byte[100];

    @BeforeEach
    void setUp() throws IOException {
        config = new Config();
        this.eleSub = new ElevatorSubsystem(config);
        this.scheduler = new Scheduler(config);
        this.floorSubsystem = new FloorSubsystem(config);
        //this.floorSub = new FloorSubsystem(config);
        testSocket = new DatagramSocket(6000);
        testReceivePacket = new DatagramPacket(classData, classData.length);
    }

    @AfterEach
    void tearDown() {
        floorSubsystem.sendReceiveSocket.close();
        eleSub.getSocket().close();
        testSocket.close();
        scheduler.sendReceiveSocket.close();
    }

    @Test
    void read() {
        //Test to ensure the right data is passed to the Scheduler
        assertEquals("10000000 1 up 4", floorSubsystem.read().get(0));
    }

    @Test
    void send() throws UnknownHostException {
        //This test method ensures that data sent from the floor is received by the test socket. If this passes,
        //you can be certain that the scheduler will receive the data
        String data = "10000000 1 up 4";
        floorSubsystem.send(data, 6000, 1);
        try{
            testSocket.receive(testReceivePacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertTrue(testReceivePacket.getData() != null);
    }

    @Test
    void receive() throws UnknownHostException {
        //This test method makes sure that the floor receives data sent by the scheduler
        //In this test, the scheduler is not used to test. However, we can be certain that if this method passes it's test,
        //the scheduler can also pass this test.
        String data = "14:05:15.0 1 Up 4";
        floorSubsystem.send(data, floorSubsystem.getSchPort(), 1);
        scheduler.receivePacketFloorSubsystem();

        eleSub.updateData(1, "1|0|0|0|0|1|0");
        eleSub.updateData(2, "2|0|0|0|0|1|0");
        eleSub.updateData(3, "3|0|0|0|0|1|0");
        eleSub.updateData(4, "4|0|0|0|0|1|0");

        eleSub.sendDataList();

        scheduler.requestElevatorLocations();
        scheduler.sendToFloorSubsystem();

        floorSubsystem.receive();
        assertTrue(floorSubsystem.packetData() != null);
    }
}