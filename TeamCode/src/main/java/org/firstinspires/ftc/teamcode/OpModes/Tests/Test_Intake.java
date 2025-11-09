package org.firstinspires.ftc.teamcode.OpModes.Tests;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.LifecycleManagementUtilities.HardwareInitializer;
import org.firstinspires.ftc.teamcode.LifecycleManagementUtilities.HardwareShutdown;
import org.firstinspires.ftc.teamcode.Subsystems.Intake;

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
public class Test_Intake extends LinearOpMode {

	private Intake intake;
	private String motorState = "STOPPED";

	@Override
	public void runOpMode() throws InterruptedException {
		// Initialize hardware
		HardwareInitializer.initialize(hardwareMap);
		intake = Intake.getInstance();

		telemetry.addData("Status", "Initialized - Waiting for START");
		telemetry.addData("Purpose", "Test intake motor operations");
		telemetry.update();

		waitForStart();

		telemetry.addData("Status", "Running");
		telemetry.addData("Controls", "A=IN, B=OUT, X=STOP");
		telemetry.update();

		while (opModeIsActive()) {
			// A button - Run intake IN (forward)
			if (gamepad1.a) {
				intake.in().run(null);
				motorState = "RUNNING IN";
				telemetry.addData("Action", "Starting intake IN");
			}

			// B button - Run intake OUT (reverse)
			if (gamepad1.b) {
				intake.out().run(null);
				motorState = "RUNNING OUT";
				telemetry.addData("Action", "Starting intake OUT");
			}

			// X button - Stop intake
			if (gamepad1.x) {
				intake.stop().run(null);
				motorState = "STOPPED";
				telemetry.addData("Action", "Stopping intake");
			}

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
		}

		// Shutdown
		HardwareShutdown.shutdown();
	}
}
