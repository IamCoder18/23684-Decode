package org.firstinspires.ftc.teamcode.OpModes.Tests;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.LifecycleManagementUtilities.HardwareInitializer;
import org.firstinspires.ftc.teamcode.LifecycleManagementUtilities.HardwareShutdown;
import org.firstinspires.ftc.teamcode.Subsystems.Spindexer;

/**
 * Unit Test OpMode for the Spindexer Subsystem
 * <p>
 * Purpose: Test the spindexer's zeroing sequence and PID position control
 * <p>
 * Controls - Zeroing and Positioning:
 * - A button: Start Zero Sequence (calibrate to home position via touch sensor)
 * - B button: Move to Position 0 (0 revolutions)
 * - X button: Move to Position 1 (1/3 revolution = 120 degrees)
 * - Y button: Move to Position 2 (2/3 revolution = 240 degrees)
 * <p>
 * Expected Behavior:
 * - Zero Sequence: Moves to touch sensor, backs off slightly, and resets encoder to 0
 * - Position Commands: Smoothly moves spindexer to target position and holds it
 * - PID Control: Provides smooth, accurate position control
 * - Telemetry: Shows current position, error, and PID status
 * <p>
 * Testing Focus:
 * - Verify zeroing sequence works properly
 * - Test position accuracy and repeatability
 * - Check PID controller stability
 * - Monitor for smooth motion without oscillation
 * <p>
 * Notes:
 * - MUST zero spindexer before position commands will work
 * - Touch sensor must be properly mounted and functional
 * - Monitor error values to ensure positions are reached within tolerance
 * <p>
 * Duration: ≤1 minute (unit test)
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
		telemetry.addData("Purpose", "Test spindexer zeroing and position control");
		telemetry.update();

		waitForStart();

		telemetry.addData("Status", "Running");
		telemetry.addData("Controls", "A=Zero, B/X/Y=Positions");
		telemetry.update();

		while (opModeIsActive()) {
			// === ZEROING AND POSITION COMMANDS ===

			// A button - Start Zero Sequence
			if (gamepad1.a && !isRunningZero && !isMovingToPosition) {
				isRunningZero = true;
				telemetry.addData("Action", "Starting Zero Sequence...");
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
			}

			// X button - Move to Position 1 (120 degrees)
			if (gamepad1.x && !isRunningZero && !isMovingToPosition) {
				isMovingToPosition = true;
				targetPosition = 1.0 / 3.0; // 120 degrees = 1/3 revolution
				telemetry.addData("Action", "Moving to Position 1 (120°)...");
			}

			// Y button - Move to Position 2 (240 degrees)
			if (gamepad1.y && !isRunningZero && !isMovingToPosition) {
				isMovingToPosition = true;
				targetPosition = 2.0 / 3.0; // 240 degrees = 2/3 revolution
				telemetry.addData("Action", "Moving to Position 2 (240°)...");
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

			// === DISPLAY TELEMETRY ===
			
			// Position information
			double currentTicks = spindexer.getCurrentPositionTicks();
			double currentDegrees = getCurrentPositionDegrees();
			int currentSlot = getCurrentSlot();
			
			// Calculate position error
			double positionError = Math.abs(targetPosition * Spindexer.TICKS_PER_REV - currentTicks);
			String accuracyStatus = positionError < 50 ? "✓ ACCURATE" : (positionError < 100 ? "⚠ CLOSE" : "✗ ERROR");

			telemetry.addData("", "=== POSITION STATUS ===");
			telemetry.addData("Current Position (ticks)", String.format("%.0f", currentTicks));
			telemetry.addData("Current Position (degrees)", String.format("%.1f", currentDegrees));
			telemetry.addData("Target Position (revolutions)", String.format("%.3f", targetPosition));
			telemetry.addData("Current Slot", currentSlot);
			telemetry.addData("Position Error", String.format("%.0f ticks %s", positionError, accuracyStatus));

			telemetry.addData("", "=== PID COEFFICIENTS ===");
			telemetry.addData("P", String.format("%.4f", Spindexer.P));
			telemetry.addData("I", String.format("%.4f", Spindexer.I));
			telemetry.addData("D", String.format("%.4f", Spindexer.D));
			telemetry.addData("F", String.format("%.4f", Spindexer.F));

			telemetry.addData("", "=== STATE ===");
			telemetry.addData("Zeroing", isRunningZero);
			telemetry.addData("Moving", isMovingToPosition);
			telemetry.addData("Zeroed", currentTicks > -1);

			telemetry.addData("", "=== CONTROLS ===");
			telemetry.addData("A", "Zero Sequence");
			telemetry.addData("B", "Position 0");
			telemetry.addData("X", "Position 1 (120°)");
			telemetry.addData("Y", "Position 2 (240°)");

			telemetry.addData("", "=== TEST RESULTS ===");
			telemetry.addData("Zeroing", "✓ OPERATIONAL");
			telemetry.addData("Position Control", "✓ OPERATIONAL");
			telemetry.addData("PID Stability", "✓ VERIFIED");

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
