package project;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MotorTest {
    private Motor motor;

    @BeforeEach
    void setUp() {
        motor = new Motor(1, false, false);
    }

    @Test
    void elevatorGoingUp() {
        assertEquals(false, motor.isGoingUp());
        assertEquals(false, motor.isMotorTurnedOn());
        motor.elevatorGoingUp();
        assertEquals(true, motor.isGoingUp());
        assertEquals(true, motor.isMotorTurnedOn());
    }

    @Test
    void elevatorGoingDown() {
        assertEquals(false, motor.isGoingUp());
        assertEquals(false, motor.isMotorTurnedOn());
        motor.elevatorGoingDown();
        assertEquals(false, motor.isGoingUp());
        assertEquals(true, motor.isMotorTurnedOn());
    }

    @Test
    void elevatorArrived() {

    }
}