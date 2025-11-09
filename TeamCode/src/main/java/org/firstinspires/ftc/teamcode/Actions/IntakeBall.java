package org.firstinspires.ftc.teamcode.Actions;

import androidx.annotation.NonNull;

import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.Action;

import org.firstinspires.ftc.teamcode.Subsystems.ColorDetector;
import org.firstinspires.ftc.teamcode.Subsystems.Intake;
import org.firstinspires.ftc.teamcode.Subsystems.Spindexer;
import org.firstinspires.ftc.teamcode.Subsystems.Transfer;

public class IntakeBall implements Action {
	private static final double SLOT_TOLERANCE_DEGREES = 5.0;
	private IndexState currentState = IndexState.RUN_INTAKE;
	private long waitStartTimeNanos;
	private int slotIndex;
	public IntakeBall() {
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

		// All slots filled (shouldn't happen in normal operation)
		return currentSlot;
	}

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

		switch (currentState) {
			case RUN_INTAKE:
				// Run INTAKE motor forward
				Intake.getInstance().in();
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
				Transfer.getInstance().intakeDoorForward();
				currentState = IndexState.WAIT_FOR_BALL;
				break;

			case WAIT_FOR_BALL:
				// Update color sensor readings
				ColorDetector.getInstance().update().run(packet);
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
				Transfer.getInstance().intakeDoorStop();
				long elapsedNanos = System.nanoTime() - waitStartTimeNanos;
				if (elapsedNanos >= 0.2 * 1_000_000_000) {
					// Stop intake and intake door just before done
					Intake.getInstance().stop();
					Transfer.getInstance().intakeDoorStop();
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
