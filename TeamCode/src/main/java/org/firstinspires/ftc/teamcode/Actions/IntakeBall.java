package org.firstinspires.ftc.teamcode.Actions;

import androidx.annotation.NonNull;

import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.Action;
import com.qualcomm.robotcore.util.ElapsedTime;

public class IntakeBall implements Action {
    private enum IndexState {
        RUN_INTAKE,
        MOVE_TO_NEXT_SLOT,
        RUN_INTAKE_DOOR,
        WAIT_FOR_BALL,
        WAIT_0_2_SECONDS,
        DONE
    }

    private IndexState currentState = IndexState.RUN_INTAKE;
    private ElapsedTime elapsedTime;
    private int slotIndex;
    private static final double SLOT_TOLERANCE_DEGREES = 5.0;
    private static final double TICKS_PER_REV = 8192.0;

    private final Spindexer spindexer;
    private final Transfer transfer;
    private final ColorSensors colorSensors;

    public IntakeBall(Spindexer spindexer, Transfer transfer, ColorSensors colorSensors) {
        this.spindexer = spindexer;
        this.transfer = transfer;
        this.colorSensors = colorSensors;
        
        // Find the next free slot to index into
        slotIndex = findNextFreeSlot();
    }

    private int findNextFreeSlot() {
        // Check which slots are already filled
        double currentPositionDegrees = getCurrentPositionDegrees();
        
        // Check current slot with 5 degree tolerance
        int currentSlot = (int) ((currentPositionDegrees / 120) % 3);
        double slotStartDegrees = currentSlot * 120;
        double degreesFromSlotStart = Math.abs(currentPositionDegrees - slotStartDegrees);
        if (degreesFromSlotStart > 180) {
            degreesFromSlotStart = 360 - degreesFromSlotStart;
        }
        
        if (spindexer.getBallColor(currentSlot) == BallColor.UNKNOWN && degreesFromSlotStart <= SLOT_TOLERANCE_DEGREES) {
            return currentSlot;
        }
        
        // Find next free slot
        for (int i = 0; i < 3; i++) {
            int nextSlot = (currentSlot + 1 + i) % 3;
            if (spindexer.getBallColor(nextSlot) == BallColor.UNKNOWN) {
                return nextSlot;
            }
        }
        
        // All slots filled (shouldn't happen in normal operation)
        return currentSlot;
    }

    private double getCurrentPositionDegrees() {
        double currentPositionTicks = spindexer.getCurrentPositionTicks();
        double currentPositionDegrees = (currentPositionTicks % TICKS_PER_REV) / TICKS_PER_REV * 360;
        if (currentPositionDegrees < 0) {
            currentPositionDegrees += 360;
        }
        return currentPositionDegrees;
    }

    @Override
    public boolean run(@NonNull TelemetryPacket packet) {
        packet.put("Index State", currentState.toString());
        packet.put("Slot Index", slotIndex);

        switch (currentState) {
            case RUN_INTAKE:
                transfer.transferForward();
                currentState = IndexState.MOVE_TO_NEXT_SLOT;
                break;

            case MOVE_TO_NEXT_SLOT: {
                // Keep intake running
                transfer.transferForward();
                double currentPositionDegrees = getCurrentPositionDegrees();
                double slotStartDegrees = slotIndex * 120;
                double targetRevolutions = slotStartDegrees / 360;
                
                // Set target position and check if reached
                double targetTicks = targetRevolutions * TICKS_PER_REV;
                spindexer.setTargetPosition(targetRevolutions);
                double error = targetTicks - spindexer.getCurrentPositionTicks();
                if (Math.abs(error) < 50) {
                    currentState = IndexState.RUN_INTAKE_DOOR;
                }
                break;
            }

            case RUN_INTAKE_DOOR:
                transfer.intakeDoorForward();
                currentState = IndexState.WAIT_FOR_BALL;
                break;

            case WAIT_FOR_BALL:
                // Keep intake running while waiting for ball
                transfer.transferForward();
                // Update color sensor readings
                colorSensors.update();
                if (colorSensors.isGreen || colorSensors.isPurple) {
                    BallColor detectedColor = colorSensors.isGreen ? BallColor.GREEN : BallColor.PURPLE;
                    spindexer.setBallColor(slotIndex, detectedColor);
                    packet.put("Detected Color", detectedColor.toString());
                    elapsedTime = new ElapsedTime();
                    currentState = IndexState.WAIT_0_2_SECONDS;
                }
                break;

            case WAIT_0_2_SECONDS:
                // Keep intake running during wait period
                transfer.transferForward();
                if (elapsedTime.seconds() >= 0.2) {
                    // Stop intake and intake door just before done
                    transfer.transferStop();
                    transfer.intakeDoorStop();
                    currentState = IndexState.DONE;
                }
                break;

            case DONE:
                return true; // Action is complete
        }
        return false; // Action is still running
    }
}
