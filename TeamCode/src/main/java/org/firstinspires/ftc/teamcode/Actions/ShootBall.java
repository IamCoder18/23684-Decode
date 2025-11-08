package org.firstinspires.ftc.teamcode.Actions;

import androidx.annotation.NonNull;

import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.Action;
import org.firstinspires.ftc.teamcode.Subsystems.Spindexer;

public class ShootBall implements Action {

    private final BallColor requestedColor;
    private static final double SLOT_TOLERANCE_DEGREES = 5.0;
    private static final double SHOOTER_ALIGNMENT_DEGREES = 131.011;
    private static final double SLOT_0_CENTER = 0.0;
    private static final double SLOT_1_CENTER = 120.0;
    private static final double SLOT_2_CENTER = 240.0;
    private static final double[] SLOT_CENTERS = {SLOT_0_CENTER, SLOT_1_CENTER, SLOT_2_CENTER};

    // Constructor without color preference (any color)
    public ShootBall() {
        this.requestedColor = null;
    }

    // Constructor with optional color preference
    public ShootBall(BallColor requestedColor) {
        this.requestedColor = requestedColor;
    }

    @Override
    public boolean run(@NonNull TelemetryPacket packet) {
        double currentPositionDegrees = getCurrentPositionDegrees();
        int targetSlot = findTargetSlot(currentPositionDegrees);

        // Calculate target degrees to align slot center with shooter
        double targetDegrees = (SLOT_CENTERS[targetSlot] + SHOOTER_ALIGNMENT_DEGREES) % 360.0;
        double targetRevolutions = targetDegrees / 360.0;

        // Set the target for the PID controller
        Spindexer.getInstance().setTargetPosition(targetRevolutions);

        // Check if position is reached (within tolerance)
        double currentTicks = Spindexer.getInstance().getCurrentPositionTicks();
        double targetTicks = targetRevolutions * Spindexer.TICKS_PER_REV;
        double error = targetTicks - currentTicks;

        packet.put("ShootBall Target Slot", targetSlot);
        packet.put("ShootBall Target Degrees", targetDegrees);
        packet.put("ShootBall Current Degrees", currentPositionDegrees);
        packet.put("ShootBall Error", error);

        return Math.abs(error) < 50; // 50 ticks tolerance (matching toPosition)
    }

    private int findTargetSlot(double currentPositionDegrees) {
        // Priority 1: If a color is requested, find a slot with that color within tolerance
        if (requestedColor != null && requestedColor != BallColor.UNKNOWN) {
            for (int i = 0; i < 3; i++) {
                if (Spindexer.getInstance().getBallColor(i) == requestedColor) {
                    double degreesFromSlotCenter = getDegreesFromSlotCenter(currentPositionDegrees, SLOT_CENTERS[i]);
                    if (degreesFromSlotCenter <= SLOT_TOLERANCE_DEGREES) {
                        return i;
                    }
                }
            }
        }

        // Priority 2: Find the most convenient slot (within tolerance, prefer not moving)
        // First check if we're already within tolerance of any slot
        for (int i = 0; i < 3; i++) {
            double degreesFromSlotCenter = getDegreesFromSlotCenter(currentPositionDegrees, SLOT_CENTERS[i]);
            if (degreesFromSlotCenter <= SLOT_TOLERANCE_DEGREES) {
                return i; // Already positioned at this slot, no movement needed
            }
        }

        // If not within tolerance of any slot, find the closest one
        int closestSlot = 0;
        double closestDistance = getDegreesFromSlotCenter(currentPositionDegrees, SLOT_CENTERS[0]);

        for (int i = 1; i < 3; i++) {
            double distance = getDegreesFromSlotCenter(currentPositionDegrees, SLOT_CENTERS[i]);
            if (distance < closestDistance) {
                closestDistance = distance;
                closestSlot = i;
            }
        }

        return closestSlot;
    }

    /**
     * Calculate the shortest angular distance from current position to a slot center.
     * Accounts for wrap-around at 360 degrees.
     */
    private double getDegreesFromSlotCenter(double currentDegrees, double slotCenterDegrees) {
        double difference = Math.abs(currentDegrees - slotCenterDegrees);
        if (difference > 180) {
            difference = 360 - difference;
        }
        return difference;
    }

    private double getCurrentPositionDegrees() {
        double currentPositionTicks = Spindexer.getInstance().getCurrentPositionTicks();
        double currentPositionDegrees = (currentPositionTicks % Spindexer.TICKS_PER_REV) / Spindexer.TICKS_PER_REV * 360;
        if (currentPositionDegrees < 0) {
            currentPositionDegrees += 360;
        }
        return currentPositionDegrees;
    }
}
