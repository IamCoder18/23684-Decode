package org.firstinspires.ftc.teamcode.OpModes.Tuning;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.LifecycleManagementUtilities.HardwareInitializer;
import org.firstinspires.ftc.teamcode.LifecycleManagementUtilities.HardwareShutdown;
import org.firstinspires.ftc.teamcode.Subsystems.Shooter;
import org.firstinspires.ftc.teamcode.Utilities.ActionScheduler;

/**
 * Tuning OpMode for the Shooter Subsystem
 * 
 * Purpose: Synchronize upper and lower motors via offsets using real-time adjustment
 * 
 * FTC Dashboard Constants:
 * - RUN_POWER: [-1.0, 1.0] (default: 1.0)
 * - UPPER_OFFSET: [-1.0, 1.0] (default: 0.0)
 * - LOWER_OFFSET: [-1.0, 1.0] (default: 0.0)
 * - STOP_POWER: [-1.0, 1.0] (default: 0.0)
 * 
 * Controls:
 * - DPAD UP/DOWN: Adjust RUN_POWER (±0.05)
 * - LB/RB: Adjust UPPER_OFFSET (±0.01)
 * - LT/RT: Adjust LOWER_OFFSET (±0.01)
 * - A: Run both motors
 * - B: Stop both motors
 * - Y: Reset to defaults
 * 
 * Expected Behavior:
 * - Real-time motor speed synchronization
 * - Motor RPM display in telemetry
 * - Clear visual feedback on speed differences
 * - Fine-grained offset adjustment for precision tuning
 * 
 * Notes:
 * - Values update in real-time through FTC Dashboard
 * - No timeout - operator can tune as long as needed
 * - Focus on single subsystem concern
 * - Use when motors are spinning at different speeds
 */
@TeleOp(name = "Tune_Shooter", group = "Tuning")
public class Tune_Shooter extends OpMode {

	private Shooter shooter;
	private ActionScheduler scheduler;
	private static final double POWER_STEP = 0.05;
	private static final double OFFSET_STEP = 0.01;

	@Override
	public void init() {
		// Initialize hardware
		HardwareInitializer.initialize(hardwareMap);
		shooter = Shooter.getInstance();
		scheduler = ActionScheduler.getInstance();

		telemetry.addData("Status", "Initialized - Waiting for START");
		telemetry.addData("Purpose", "Tune shooter motor synchronization");
		telemetry.addData("Note", "Use FTC Dashboard to adjust values in real-time");
		telemetry.update();
	}

	@Override
	public void start() {
		telemetry.addData("Status", "Running");
		telemetry.addData("Instructions", "Press A=RUN, B=STOP, Y=RESET");
		telemetry.update();
	}

	@Override
	public void loop() {
		// === MOTOR CONTROL ===

		// A button - Run both motors
		if (gamepad1.a) {
			scheduler.schedule(shooter.run());
			telemetry.addData("Motors", "RUNNING");
		}

		// B button - Stop both motors
		if (gamepad1.b) {
			scheduler.schedule(shooter.stop());
			telemetry.addData("Motors", "STOPPED");
		}

		// Y button - Reset to defaults
		if (gamepad1.y) {
			Shooter.RUN_POWER = 1.0;
			Shooter.UPPER_OFFSET = 0.0;
			Shooter.LOWER_OFFSET = 0.0;
			Shooter.STOP_POWER = 0.0;
			telemetry.addData("Action", "Reset to defaults");
		}

		// === FINE TUNING ADJUSTMENTS ===

		// DPAD UP - Increase RUN_POWER
		if (gamepad1.dpad_up) {
			Shooter.RUN_POWER = Math.min(Shooter.RUN_POWER + POWER_STEP, 1.0);
			telemetry.addData("RUN_POWER", "Adjusted to " + String.format("%.2f", Shooter.RUN_POWER));
		}

		// DPAD DOWN - Decrease RUN_POWER
		if (gamepad1.dpad_down) {
			Shooter.RUN_POWER = Math.max(Shooter.RUN_POWER - POWER_STEP, 0.0);
			telemetry.addData("RUN_POWER", "Adjusted to " + String.format("%.2f", Shooter.RUN_POWER));
		}

		// LB - Decrease UPPER_OFFSET
		if (gamepad1.left_bumper) {
			Shooter.UPPER_OFFSET = Math.max(Shooter.UPPER_OFFSET - OFFSET_STEP, -0.5);
			telemetry.addData("UPPER_OFFSET", "Adjusted to " + String.format("%.3f", Shooter.UPPER_OFFSET));
		}

		// RB - Increase UPPER_OFFSET
		if (gamepad1.right_bumper) {
			Shooter.UPPER_OFFSET = Math.min(Shooter.UPPER_OFFSET + OFFSET_STEP, 0.5);
			telemetry.addData("UPPER_OFFSET", "Adjusted to " + String.format("%.3f", Shooter.UPPER_OFFSET));
		}

		// LT - Decrease LOWER_OFFSET
		if (gamepad1.left_trigger > 0.5) {
			Shooter.LOWER_OFFSET = Math.max(Shooter.LOWER_OFFSET - OFFSET_STEP, -0.5);
			telemetry.addData("LOWER_OFFSET", "Adjusted to " + String.format("%.3f", Shooter.LOWER_OFFSET));
		}

		// RT - Increase LOWER_OFFSET
		if (gamepad1.right_trigger > 0.5) {
			Shooter.LOWER_OFFSET = Math.min(Shooter.LOWER_OFFSET + OFFSET_STEP, 0.5);
			telemetry.addData("LOWER_OFFSET", "Adjusted to " + String.format("%.3f", Shooter.LOWER_OFFSET));
		}

		// === UPDATE SCHEDULER ===
		scheduler.update();

		// === TELEMETRY DISPLAY ===

		// Calculate motor speeds (simulated RPM for display)
		double upperSpeed = Math.abs(Shooter.RUN_POWER + Shooter.UPPER_OFFSET) * 2800; // Assume max 2800 RPM
		double lowerSpeed = Math.abs(Shooter.RUN_POWER + Shooter.LOWER_OFFSET) * 2800;
		double speedDifference = ((upperSpeed - lowerSpeed) / Math.max(upperSpeed, lowerSpeed)) * 100;
		String syncStatus = Math.abs(speedDifference) < 2.0 ? "✓ SYNCED" : (speedDifference > 0 ? "↓ UPPER FAST" : "↑ UPPER SLOW");

		telemetry.addData("", "=== MOTOR SPEEDS ===");
		telemetry.addData("Upper Motor", String.format("%.0f RPM", upperSpeed));
		telemetry.addData("Lower Motor", String.format("%.0f RPM", lowerSpeed));
		telemetry.addData("Difference", String.format("%.2f%% %s", speedDifference, syncStatus));

		telemetry.addData("", "=== DASHBOARD VALUES ===");
		telemetry.addData("RUN_POWER", String.format("%.2f (DPAD ↑↓)", Shooter.RUN_POWER));
		telemetry.addData("UPPER_OFFSET", String.format("%.3f (LB/RB)", Shooter.UPPER_OFFSET));
		telemetry.addData("LOWER_OFFSET", String.format("%.3f (LT/RT)", Shooter.LOWER_OFFSET));
		telemetry.addData("STOP_POWER", String.format("%.2f", Shooter.STOP_POWER));

		telemetry.addData("", "=== CONTROLS ===");
		telemetry.addData("A: RUN    B: STOP    Y: RESET", "");
		telemetry.addData("Fine tune with gamepad", "Dashboard for precision");

		telemetry.update();
	}

	@Override
	public void stop() {
		// Shutdown
		HardwareShutdown.shutdown();
	}
}
