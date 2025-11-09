package org.firstinspires.ftc.teamcode.OpModes.Tests;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.Actions.BallColor;
import org.firstinspires.ftc.teamcode.LifecycleManagementUtilities.HardwareInitializer;
import org.firstinspires.ftc.teamcode.LifecycleManagementUtilities.HardwareShutdown;
import org.firstinspires.ftc.teamcode.Subsystems.Spindexer;

/**
 * Unit Test OpMode for the Spindexer Subsystem
 * <p>
 * Purpose: Tests the spindexer's zeroing sequence, PID position control, and storage management
 * <p>
 * Controls - Zeroing and Positioning:
 * - A button: Start Zero Sequence (calibrate to home position via touch sensor)
 * - B button: Move to Position 0 (0 revolutions)
 * - X button: Move to Position 1 (1/3 revolution = 120 degrees)
 * - Y button: Move to Position 2 (2/3 revolution = 240 degrees)
 * <p>
 * Controls - Ball Color Management:
 * - DPAD UP: Set current slot to GREEN
 * - DPAD DOWN: Set current slot to PURPLE
 * - DPAD LEFT: Set current slot to UNKNOWN
 * - DPAD RIGHT: Clear all slot colors
 * <p>
 * Controls - Tuning:
 * - LB: Decrease P coefficient (sensitivity)
 * - RB: Increase P coefficient (sensitivity)
 * - LT: Decrease D coefficient (damping)
 * - RT: Increase D coefficient (damping)
 * <p>
 * Expected Behavior:
 * - Zero Sequence: Moves to touch sensor, backs off slightly, and resets encoder to 0
 * - Position Commands: Smoothly moves spindexer to target position and holds it
 * - Color Storage: Tracks which balls are in each of the 3 slots
 * - PID Tuning: Allows adjustment of controller gains for smooth motion
 * - Telemetry: Shows current position, error, state, and stored ball colors
 * <p>
 * Notes:
 * - MUST call Zero Sequence before position commands will work
 * - Touch sensor must be properly mounted and functional
 * - Encoder readings should be checked against physical rotation
 * - Fine-tune P and D values for smooth, responsive motion
 * - Monitor error values to ensure positions are reached within 50 tick tolerance
 */
@TeleOp(name = "Test_Spindexer", group = "Unit Tests")
public class Test_Spindexer extends LinearOpMode {

	private Spindexer spindexer;
	private boolean isRunningZero = false;
	private boolean isMovingToPosition = false;
	private double targetPosition = 0;

	@Override
	public void runOpMode() throws InterruptedException {
		// Initialize hardware
		HardwareInitializer.initialize(hardwareMap);
		spindexer = Spindexer.getInstance();

		telemetry.addData("Status", "Initialized - Waiting for START");
		telemetry.update();

		waitForStart();

		telemetry.addData("Status", "Running");
		telemetry.update();

		while (opModeIsActive()) {
			// === ZEROING AND POSITION COMMANDS ===

			// A button - Start Zero Sequence
			if (gamepad1.a && !isRunningZero && !isMovingToPosition) {
				isRunningZero = true;
				telemetry.addData("Action", "Starting Zero Sequence...");
				telemetry.update();
			}

			// Run zero sequence if active
			if (isRunningZero) {
				boolean zeroComplete = spindexer.zero().run(null);
				if (zeroComplete) {
					isRunningZero = false;
					telemetry.addData("Action", "Zero Sequence Complete!");
					Thread.sleep(500); // Allow user to see message
				}
			}

			// B button - Move to Position 0
			if (gamepad1.b && !isRunningZero && !isMovingToPosition) {
				isMovingToPosition = true;
				targetPosition = 0;
				telemetry.addData("Action", "Moving to Position 0...");
				telemetry.update();
			}

			// X button - Move to Position 1 (120 degrees)
			if (gamepad1.x && !isRunningZero && !isMovingToPosition) {
				isMovingToPosition = true;
				targetPosition = 1.0 / 3.0; // 120 degrees = 1/3 revolution
				telemetry.addData("Action", "Moving to Position 1 (120°)...");
				telemetry.update();
			}

			// Y button - Move to Position 2 (240 degrees)
			if (gamepad1.y && !isRunningZero && !isMovingToPosition) {
				isMovingToPosition = true;
				targetPosition = 2.0 / 3.0; // 240 degrees = 2/3 revolution
				telemetry.addData("Action", "Moving to Position 2 (240°)...");
				telemetry.update();
			}

			// Run position movement if active
			if (isMovingToPosition) {
				boolean positionReached = spindexer.toPosition(targetPosition).run(null);
				if (positionReached) {
					isMovingToPosition = false;
					telemetry.addData("Action", "Position reached!");
					Thread.sleep(300); // Allow user to see message
				}
			}

			// Update PID loop (must be called frequently)
			spindexer.update();

			// === BALL COLOR MANAGEMENT ===

			int currentSlot = getCurrentSlot();

			// DPAD UP - Set current slot to GREEN
			if (gamepad1.dpad_up) {
				spindexer.setBallColor(currentSlot, BallColor.GREEN);
				telemetry.addData("Color Set", "Slot " + currentSlot + " = GREEN");
				Thread.sleep(200); // Debounce
			}

			// DPAD DOWN - Set current slot to PURPLE
			if (gamepad1.dpad_down) {
				spindexer.setBallColor(currentSlot, BallColor.PURPLE);
				telemetry.addData("Color Set", "Slot " + currentSlot + " = PURPLE");
				Thread.sleep(200); // Debounce
			}

			// DPAD LEFT - Set current slot to UNKNOWN
			if (gamepad1.dpad_left) {
				spindexer.setBallColor(currentSlot, BallColor.UNKNOWN);
				telemetry.addData("Color Set", "Slot " + currentSlot + " = UNKNOWN");
				Thread.sleep(200); // Debounce
			}

			// DPAD RIGHT - Clear all slots
			if (gamepad1.dpad_right) {
				for (int i = 0; i < 3; i++) {
					spindexer.setBallColor(i, BallColor.UNKNOWN);
				}
				telemetry.addData("Color Set", "All slots cleared");
				Thread.sleep(200); // Debounce
			}

			// === PID TUNING ===

			// Left Bumper - Decrease P coefficient
			if (gamepad1.left_bumper) {
				Spindexer.P = Math.max(Spindexer.P - 0.001, 0.0);
				telemetry.addData("P adjusted to", Spindexer.P);
				Thread.sleep(100); // Debounce
			}

			// Right Bumper - Increase P coefficient
			if (gamepad1.right_bumper) {
				Spindexer.P = Spindexer.P + 0.001;
				telemetry.addData("P adjusted to", Spindexer.P);
				Thread.sleep(100); // Debounce
			}

			// Left Trigger - Decrease D coefficient
			if (gamepad1.left_trigger > 0.5) {
				Spindexer.D = Math.max(Spindexer.D - 0.001, 0.0);
				telemetry.addData("D adjusted to", Spindexer.D);
				Thread.sleep(100); // Debounce
			}

			// Right Trigger - Increase D coefficient
			if (gamepad1.right_trigger > 0.5) {
				Spindexer.D = Spindexer.D + 0.001;
				telemetry.addData("D adjusted to", Spindexer.D);
				Thread.sleep(100); // Debounce
			}

			// === DISPLAY TELEMETRY ===
			telemetry.addData("", "--- POSITION ---");
			telemetry.addData("Current Position (ticks)", spindexer.getCurrentPositionTicks());
			telemetry.addData("Current Position (degrees)", getCurrentPositionDegrees());
			telemetry.addData("Target Position (revolutions)", targetPosition);
			telemetry.addData("Current Slot", currentSlot);

			telemetry.addData("", "--- STORED BALL COLORS ---");
			telemetry.addData("Slot 0", spindexer.getBallColor(0).toString());
			telemetry.addData("Slot 1", spindexer.getBallColor(1).toString());
			telemetry.addData("Slot 2", spindexer.getBallColor(2).toString());

			telemetry.addData("", "--- PID COEFFICIENTS ---");
			telemetry.addData("P", Spindexer.P);
			telemetry.addData("I", Spindexer.I);
			telemetry.addData("D", Spindexer.D);
			telemetry.addData("F", Spindexer.F);

			telemetry.addData("", "--- STATE ---");
			telemetry.addData("Zeroing", isRunningZero);
			telemetry.addData("Moving", isMovingToPosition);

			telemetry.update();
		}

		// Shutdown
		HardwareShutdown.shutdown();
	}

	/**
	 * Get the current slot index (0, 1, or 2) based on spindexer position
	 */
	private int getCurrentSlot() {
		double degrees = getCurrentPositionDegrees();
		return (int) ((degrees / 120) % 3);
	}

	/**
	 * Convert encoder ticks to degrees (0-360)
	 */
	private double getCurrentPositionDegrees() {
		double ticks = spindexer.getCurrentPositionTicks();
		double degrees = (ticks % Spindexer.TICKS_PER_REV) / Spindexer.TICKS_PER_REV * 360;
		if (degrees < 0) {
			degrees += 360;
		}
		return degrees;
	}
}
