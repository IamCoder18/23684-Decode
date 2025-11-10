package org.firstinspires.ftc.teamcode.Actions;

import androidx.annotation.NonNull;

import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.Action;

import org.firstinspires.ftc.teamcode.Subsystems.ColorDetector;
import org.firstinspires.ftc.teamcode.Subsystems.Intake;
import org.firstinspires.ftc.teamcode.Subsystems.Spindexer;
import org.firstinspires.ftc.teamcode.Subsystems.TouchDetector;
import org.firstinspires.ftc.teamcode.Subsystems.Transfer;
import org.firstinspires.ftc.teamcode.Utilities.BallColor;

/**
 * Action that intakes a ball into the next available slot of the spindexer.
 * 
 * This action performs a multi-state sequence:
 * 1. RUN_INTAKE: Turn on the intake motor
 * 2. MOVE_TO_NEXT_SLOT: Rotate spindexer to the target slot
 * 3. RUN_INTAKE_DOOR: Open the transfer door
 * 4. WAIT_FOR_BALL: Wait for TouchDetector to detect the ball
 * 5. WAIT_BALL_SETTLE: Hold position while ball settles (configurable)
 * 6. MOVE_TO_COLOR_SENSOR: Rotate spindexer to COLOR_SENSOR_LOCATION
 * 7. DETECT_COLOR: Wait for color sensor to detect the ball color
 * 8. DONE: Stop intake and door motors
 * 
 * The action automatically finds the next UNKNOWN slot and rotates to it.
 * If all slots are full, the action completes immediately.
 */
public class IntakeBall implements Action {
	private static final double SLOT_TOLERANCE_DEGREES = 5.0;
	public static double BALL_SETTLE_TIME_NANOS = 2.0 * 1_000_000_000; // 2 seconds in nanoseconds
	public static double COLOR_SENSOR_LOCATION_DEGREES = 15; // Position where spindexer moves to detect color
	public static double POSITION_ERROR_TOLERANCE_DEGREES = 1.5; // Position tolerance in degrees for spindexer movement
	private IndexState currentState = IndexState.RUN_INTAKE;
	private long waitStartTimeNanos;
	private final int slotIndex;

	/**
	 * Creates an IntakeBall action that finds and fills the next available slot.
	 */
	public IntakeBall() {
		// Find the next free slot to index into
		slotIndex = findNextFreeSlot();
	}

	/**
	 * Finds the next available slot in the spindexer.
	 * <p>
	 * Selection order:
	 * 1. Current slot if UNKNOWN and within tolerance
	 * 2. Next UNKNOWN slot in sequence
	 * 3. If all slots are EMPTY, current slot if within tolerance, otherwise next slot
	 * 4. -1 if all slots are full (not EMPTY or UNKNOWN)
	 * 
	 * @return slot index (0, 1, 2) or -1 if no available slots
	 */
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

		if (Spindexer.getInstance().getBallColor(currentSlot) == BallColor.UNKNOWN && degreesFromSlotStart <= SLOT_TOLERANCE_DEGREES) {
			return currentSlot;
		}

		// Find next free slot with a ball detected (UNKNOWN)
		for (int i = 0; i < 3; i++) {
			int nextSlot = (currentSlot + 1 + i) % 3;
			if (Spindexer.getInstance().getBallColor(nextSlot) == BallColor.UNKNOWN) {
				return nextSlot;
			}
		}

		// Check if all slots are EMPTY
		boolean allEmpty = true;
		for (int i = 0; i < 3; i++) {
			if (Spindexer.getInstance().getBallColor(i) != BallColor.EMPTY) {
				allEmpty = false;
				break;
			}
		}

		// If all slots are empty, return current slot if within tolerance, otherwise next slot
		if (allEmpty) {
			if (degreesFromSlotStart <= SLOT_TOLERANCE_DEGREES) {
				return currentSlot;
			} else {
				return (currentSlot + 1) % 3;
			}
		}

		// All slots filled - no free slot available
		return -1;
	}

	/**
	 * Converts the spindexer encoder ticks to degrees (0-360).
	 * 
	 * @return current position in degrees (0-360)
	 */
	private double getCurrentPositionDegrees() {
		double currentPositionTicks = Spindexer.getInstance().getCurrentPositionTicks();
		double currentPositionDegrees = (currentPositionTicks % Spindexer.TICKS_PER_REV) / Spindexer.TICKS_PER_REV * 360;
		if (currentPositionDegrees < 0) {
			currentPositionDegrees += 360;
		}
		return currentPositionDegrees;
	}

	@Override
	public boolean run(@NonNull TelemetryPacket packet) {
		packet.put("Index State", currentState.toString());
		packet.put("Slot Index", slotIndex);

		// If no free slot is available, stop the action
		if (slotIndex == -1) {
			packet.put("Intake Status", "All slots filled - cannot intake");
			return true;
		}

		switch (currentState) {
		case RUN_INTAKE:
			// Run INTAKE motor forward
			// InstantAction executes once, intake continues running via motor setPower mechanism
			Intake.getInstance().in().run(packet);
			currentState = IndexState.MOVE_TO_NEXT_SLOT;
			break;

		case MOVE_TO_NEXT_SLOT: {
			double slotStartDegrees = slotIndex * 120;
			double targetRevolutions = slotStartDegrees / 360;

			// Set target position and check if reached
			double targetTicks = targetRevolutions * Spindexer.TICKS_PER_REV;
			Spindexer.getInstance().setTargetPosition(targetRevolutions);
			double error = targetTicks - Spindexer.getInstance().getCurrentPositionTicks();
			double errorToleranceTicks = POSITION_ERROR_TOLERANCE_DEGREES * Spindexer.TICKS_PER_REV / 360.0;
			if (Math.abs(error) < errorToleranceTicks) {
				currentState = IndexState.RUN_INTAKE_DOOR;
			}
			break;
		}

		case RUN_INTAKE_DOOR:
			Transfer.getInstance().intakeDoorForward().run(packet);
			currentState = IndexState.WAIT_FOR_BALL;
			break;

		case WAIT_FOR_BALL:
			// Check touch detector to see if ball has been detected
			if (TouchDetector.getInstance().detected) {
				// Ball detected - mark slot as UNKNOWN and start settle timer
				Spindexer.getInstance().setBallColor(slotIndex, BallColor.UNKNOWN);
				waitStartTimeNanos = System.nanoTime();
				currentState = IndexState.WAIT_BALL_SETTLE;
			}
			break;

		case WAIT_BALL_SETTLE:
			// Keep intake door open during wait period
			Transfer.getInstance().intakeDoorForward().run(packet);
			long elapsedNanos = System.nanoTime() - waitStartTimeNanos;
			if (elapsedNanos >= BALL_SETTLE_TIME_NANOS) {
				currentState = IndexState.MOVE_TO_COLOR_SENSOR;
			}
			break;

		case MOVE_TO_COLOR_SENSOR: {
			double colorSensorRevolutions = COLOR_SENSOR_LOCATION_DEGREES / 360.0;
			double targetTicks = colorSensorRevolutions * Spindexer.TICKS_PER_REV;
			Spindexer.getInstance().setTargetPosition(colorSensorRevolutions);
			double error = targetTicks - Spindexer.getInstance().getCurrentPositionTicks();
			double errorToleranceTicks = POSITION_ERROR_TOLERANCE_DEGREES * Spindexer.TICKS_PER_REV / 360.0;
			if (Math.abs(error) < errorToleranceTicks) {
				currentState = IndexState.DETECT_COLOR;
			}
			break;
		}

		case DETECT_COLOR:
			// Check color sensor readings (ColorDetector.update() is called in main loop)
			if (ColorDetector.getInstance().isGreen || ColorDetector.getInstance().isPurple) {
				BallColor detectedColor = ColorDetector.getInstance().isGreen ? BallColor.GREEN : BallColor.PURPLE;
				Spindexer.getInstance().setBallColor(slotIndex, detectedColor);
				packet.put("Detected Color", detectedColor.toString());
				// Stop intake and door motors
				Intake.getInstance().stop().run(packet);
				Transfer.getInstance().intakeDoorStop().run(packet);
				currentState = IndexState.DONE;
			}
			break;

		case DONE:
			return true; // Action is complete
		}
		return false; // Action is still running
	}

	private enum IndexState {
		RUN_INTAKE,
		MOVE_TO_NEXT_SLOT,
		RUN_INTAKE_DOOR,
		WAIT_FOR_BALL,
		WAIT_BALL_SETTLE,
		MOVE_TO_COLOR_SENSOR,
		DETECT_COLOR,
		DONE
	}
}
