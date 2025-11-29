package org.firstinspires.ftc.teamcode.Actions;

import androidx.annotation.NonNull;

import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.Action;

import org.firstinspires.ftc.teamcode.Subsystems.Spindexer;
import org.firstinspires.ftc.teamcode.Utilities.BallColor;

/**
 * Action that aligns and shoots a ball from the spindexer.
 * <p>
 * This action:
 * - Determines which slot contains the target ball (or closest ball if no color preference)
 * - Calculates the rotation needed to align the slot with the shooter
 * - Commands the spindexer to rotate to that position
 * - Returns true while the spindexer is still moving, false when aligned within tolerance
 * <p>
 * Optional color filtering allows shooting specific colored balls only.
 * If no ball of the requested color is found, the closest ball is shot instead.
 */
public class ShootBall implements Action {

	private static final double SLOT_TOLERANCE_DEGREES = 5.0;
	private static final double SHOOTER_ALIGNMENT_DEGREES = 131.011;
	private static final double POSITION_ERROR_TOLERANCE_TICKS = 50.0; // Extracted magic number from line 71
	private static final double SLOT_0_CENTER = 0.0;
	private static final double SLOT_1_CENTER = 120.0;
	private static final double SLOT_2_CENTER = 240.0;
	private static final double[] SLOT_CENTERS = {SLOT_0_CENTER, SLOT_1_CENTER, SLOT_2_CENTER};
	private final BallColor requestedColor;

	/**
	 * Creates a ShootBall action that shoots any available ball.
	 */
	public ShootBall() {
		this.requestedColor = null;
	}

	/**
	 * Creates a ShootBall action with a color preference.
	 *
	 * @param requestedColor the color of ball to shoot, or null for any color
	 */
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
		double currentTicks = Spindexer.getInstance().getPosition();
		double targetTicks = targetRevolutions * Spindexer.TICKS_PER_REV;
		double error = targetTicks - currentTicks;

		packet.put("ShootBall Target Slot", targetSlot);
		packet.put("ShootBall Target Degrees", targetDegrees);
		packet.put("ShootBall Current Degrees", currentPositionDegrees);
		packet.put("ShootBall Error", error);

		return Math.abs(error) >= POSITION_ERROR_TOLERANCE_TICKS; // Returns true while moving, false when within tolerance
	}

	/**
	 * Determines which slot to shoot based on color preference and proximity.
	 * <p>
	 * Selection priority:
	 * 1. If color requested: find a matching color within tolerance
	 * 2. Find any slot already within tolerance (no movement needed)
	 * 3. Find the closest slot
	 *
	 * @param currentPositionDegrees current spindexer rotation in degrees
	 * @return slot index (0, 1, or 2) to shoot
	 */
	private int findTargetSlot(double currentPositionDegrees) {
		// Priority 1: If a color is requested, find a slot with that color.
		if (requestedColor != null && requestedColor != BallColor.UNKNOWN) {
			for (int i = 0; i < 3; i++) {
				if (Spindexer.getInstance().getBallColor(i) == requestedColor) {
					return i; // Found the requested color, select this slot.
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
	 * Calculates the shortest angular distance from current position to a slot center.
	 * Accounts for wrap-around at 360 degrees.
	 *
	 * @param currentDegrees    current spindexer position in degrees (0-360)
	 * @param slotCenterDegrees target slot center in degrees
	 * @return shortest angular distance in degrees
	 */
	private double getDegreesFromSlotCenter(double currentDegrees, double slotCenterDegrees) {
		double difference = Math.abs(currentDegrees - slotCenterDegrees);
		if (difference > 180) {
			difference = 360 - difference;
		}
		return difference;
	}

	/**
	 * Converts the spindexer encoder ticks to degrees (0-360).
	 *
	 * @return current position in degrees (0-360)
	 */
	private double getCurrentPositionDegrees() {
		double currentPositionTicks = Spindexer.getInstance().getPosition();
		double currentPositionDegrees = (currentPositionTicks % Spindexer.TICKS_PER_REV) / Spindexer.TICKS_PER_REV * 360;
		if (currentPositionDegrees < 0) {
			currentPositionDegrees += 360;
		}
		return currentPositionDegrees;
	}
}
