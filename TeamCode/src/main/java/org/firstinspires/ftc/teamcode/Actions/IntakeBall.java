package org.firstinspires.ftc.teamcode.Actions;

import androidx.annotation.NonNull;

import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.Action;

import org.firstinspires.ftc.teamcode.Subsystems.ColorDetector;
import org.firstinspires.ftc.teamcode.Subsystems.Intake;
import org.firstinspires.ftc.teamcode.Subsystems.Spindexer;
import org.firstinspires.ftc.teamcode.Subsystems.Transfer;
import org.firstinspires.ftc.teamcode.Utilities.BallColor;

/**
 * Action that intakes a ball into the next available slot of the spindexer.
 * 
 * This action performs a multi-state sequence:
 * 1. RUN_INTAKE: Turn on the intake motor
 * 2. MOVE_TO_NEXT_SLOT: Rotate spindexer to the target slot
 * 3. RUN_INTAKE_DOOR: Open the transfer door
 * 4. WAIT_FOR_BALL: Wait for color sensor to detect the ball
 * 5. WAIT_0_2_SECONDS: Hold position while ball settles (0.2 seconds)
 * 6. DONE: Stop intake and door motors
 * 
 * The action automatically finds the next empty slot and rotates to it.
 * If all slots are full, the action completes immediately.
 */
public class IntakeBall implements Action {
	private static final double SLOT_TOLERANCE_DEGREES = 5.0;
	private IndexState currentState = IndexState.RUN_INTAKE;
	private long waitStartTimeNanos;
	private int slotIndex;

	/**
	 * Creates an IntakeBall action that finds and fills the next available slot.
	 */
	public IntakeBall() {
		// Find the next free slot to index into
		slotIndex = findNextFreeSlot();
	}

	/**
	 * Finds the next empty slot in the spindexer.
	 * 
	 * Selection order:
	 * 1. Current slot if empty and within tolerance
	 * 2. Next empty slot in sequence
	 * 3. -1 if all slots are full
	 * 
	 * @return slot index (0, 1, 2) or -1 if all slots are full
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

		// Find next free slot
		for (int i = 0; i < 3; i++) {
			int nextSlot = (currentSlot + 1 + i) % 3;
			if (Spindexer.getInstance().getBallColor(nextSlot) == BallColor.UNKNOWN) {
				return nextSlot;
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
			if (Math.abs(error) < 50) {
				currentState = IndexState.RUN_INTAKE_DOOR;
			}
			break;
		}

		case RUN_INTAKE_DOOR:
			Transfer.getInstance().intakeDoorForward().run(packet);
			currentState = IndexState.WAIT_FOR_BALL;
			break;

		case WAIT_FOR_BALL:
			// Check color sensor readings (ColorDetector.update() is called in main loop)
			if (ColorDetector.getInstance().isGreen || ColorDetector.getInstance().isPurple) {
				BallColor detectedColor = ColorDetector.getInstance().isGreen ? BallColor.GREEN : BallColor.PURPLE;
				Spindexer.getInstance().setBallColor(slotIndex, detectedColor);
				packet.put("Detected Color", detectedColor.toString());
				waitStartTimeNanos = System.nanoTime();
				currentState = IndexState.WAIT_0_2_SECONDS;
			}
			break;

		case WAIT_0_2_SECONDS:
			// Keep intake running during wait period
			Transfer.getInstance().intakeDoorStop().run(packet);
			long elapsedNanos = System.nanoTime() - waitStartTimeNanos;
			if (elapsedNanos >= 0.2 * 1_000_000_000) {
				// Stop intake and intake door just before done
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
		WAIT_0_2_SECONDS,
		DONE
	}
}
