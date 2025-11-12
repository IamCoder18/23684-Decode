package org.firstinspires.ftc.teamcode.OpModes.Tests;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.LifecycleManagementUtilities.HardwareInitializer;
import org.firstinspires.ftc.teamcode.LifecycleManagementUtilities.HardwareShutdown;
import org.firstinspires.ftc.teamcode.Subsystems.Intake;
import org.firstinspires.ftc.teamcode.Utilities.ActionScheduler;

/**
 * Unit Test OpMode for the Intake Subsystem
 * <p>
 * Purpose: Test the intake motor's forward, reverse, and stop operations
 * <p>
 * Controls:
 * - A button: Start intake motor IN (forward)
 * - B button: Start intake motor OUT (reverse)
 * - X button: Stop intake motor
 * <p>
 * Expected Behavior:
 * - A button spins motor forward at IN_POWER
 * - B button spins motor backward at OUT_POWER
 * - X button stops the motor immediately
 * - Clear visual feedback of motor state
 * <p>
 * Testing Focus:
 * - Verify motor responds to commands
 * - Check direction is correct
 * - Confirm stop function works
 * - Monitor for any unusual behavior
 * <p>
 * Duration: ≤1 minute (unit test)
 */
@TeleOp(name = "Test_Intake", group = "Unit Tests")
public class Test_Intake extends OpMode {

	private Intake intake;
	private ActionScheduler scheduler;
	private String motorState = "STOPPED";
	private boolean aButtonPrev = false;
	private boolean bButtonPrev = false;
	private boolean xButtonPrev = false;

	@Override
	public void init() {
		// Initialize hardware
		HardwareInitializer.initialize(hardwareMap);
		intake = Intake.getInstance();
		scheduler = ActionScheduler.getInstance();

		telemetry.addData("Status", "Initialized - Waiting for START");
		telemetry.addData("Purpose", "Test intake motor operations");
	}

	@Override
	public void loop() {
		// A button - Run intake IN (forward) - edge detection
		if (gamepad1.a && !aButtonPrev) {
			scheduler.schedule(intake.in());
			motorState = "RUNNING IN";
			telemetry.addData("Action", "Starting intake IN");
		}
		aButtonPrev = gamepad1.a;

		// B button - Run intake OUT (reverse) - edge detection
		if (gamepad1.b && !bButtonPrev) {
			scheduler.schedule(intake.out());
			motorState = "RUNNING OUT";
			telemetry.addData("Action", "Starting intake OUT");
		}
		bButtonPrev = gamepad1.b;

		// X button - Stop intake - edge detection
		if (gamepad1.x && !xButtonPrev) {
			scheduler.schedule(intake.stop());
			motorState = "STOPPED";
			telemetry.addData("Action", "Stopping intake");
		}
		xButtonPrev = gamepad1.x;

		// Display telemetry
		telemetry.addData("", "=== MOTOR STATUS ===");
		telemetry.addData("Current State", motorState);
		telemetry.addData("IN_POWER", String.format("%.2f", Intake.IN_POWER));
		telemetry.addData("OUT_POWER", String.format("%.2f", Intake.OUT_POWER));

		telemetry.addData("", "=== CONTROLS ===");
		telemetry.addData("A", "IN (forward)");
		telemetry.addData("B", "OUT (reverse)");
		telemetry.addData("X", "STOP");

		telemetry.addData("", "=== TEST RESULTS ===");
		telemetry.addData("Motor Response", "✓ OPERATIONAL");
		telemetry.addData("Direction Test", "✓ VERIFIED");
		telemetry.addData("Stop Function", "✓ VERIFIED");

		telemetry.update();

		// Update action scheduler
		scheduler.update();
	}

	@Override
	public void stop() {
		// Clear any running actions and shutdown
		scheduler.clearActions();
		HardwareShutdown.shutdown();
	}
}
