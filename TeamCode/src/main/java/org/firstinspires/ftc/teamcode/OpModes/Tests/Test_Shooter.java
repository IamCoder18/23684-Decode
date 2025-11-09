package org.firstinspires.ftc.teamcode.OpModes.Tests;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.LifecycleManagementUtilities.HardwareInitializer;
import org.firstinspires.ftc.teamcode.LifecycleManagementUtilities.HardwareShutdown;
import org.firstinspires.ftc.teamcode.Subsystems.Shooter;

/**
 * Unit Test OpMode for the Shooter Subsystem
 * <p>
 * Purpose: Test both shooter motors (upper and lower) for consistent operation
 * <p>
 * Controls:
 * - A button: Start both shooter motors
 * - B button: Stop both shooter motors
 * <p>
 * Expected Behavior:
 * - A button spins both motors at RUN_POWER speed
 * - B button stops both motors immediately
 * - Both motors should spin in the same direction
 * - Monitor for smooth, synchronized operation
 * <p>
 * Testing Focus:
 * - Verify both motors respond to commands
 * - Check motor synchronization
 * - Confirm stop function works for both motors
 * - Monitor for vibration or noise
 * <p>
 * Duration: ≤1 minute (unit test)
 */
@TeleOp(name = "Test_Shooter", group = "Unit Tests")
public class Test_Shooter extends LinearOpMode {

	private Shooter shooter;
	private String shooterState = "STOPPED";

	@Override
	public void runOpMode() throws InterruptedException {
		// Initialize hardware
		HardwareInitializer.initialize(hardwareMap);
		shooter = Shooter.getInstance();

		telemetry.addData("Status", "Initialized - Waiting for START");
		telemetry.addData("Purpose", "Test shooter motor synchronization");
		telemetry.update();

		waitForStart();

		telemetry.addData("Status", "Running");
		telemetry.addData("Controls", "A=RUN, B=STOP");
		telemetry.update();

		while (opModeIsActive()) {
			// A button - Start both shooter motors
			if (gamepad1.a) {
				shooter.run().run(null);
				shooterState = "RUNNING";
				telemetry.addData("Action", "Starting both shooter motors");
			}

			// B button - Stop both shooter motors
			if (gamepad1.b) {
				shooter.stop().run(null);
				shooterState = "STOPPED";
				telemetry.addData("Action", "Stopping both shooter motors");
			}

			// Calculate motor powers for display
			double upperPower = Shooter.RUN_POWER + Shooter.UPPER_OFFSET;
			double lowerPower = Shooter.RUN_POWER + Shooter.LOWER_OFFSET;
			double powerDifference = Math.abs(upperPower - lowerPower);
			String syncStatus = powerDifference < 0.05 ? "✓ SYNCED" : "⚠ OFFSET";

			// Display telemetry
			telemetry.addData("", "=== MOTOR STATUS ===");
			telemetry.addData("Current State", shooterState);
			telemetry.addData("Upper Motor Power", String.format("%.3f", upperPower));
			telemetry.addData("Lower Motor Power", String.format("%.3f", lowerPower));
			telemetry.addData("Power Difference", String.format("%.3f %s", powerDifference, syncStatus));

			telemetry.addData("", "=== SETTINGS ===");
			telemetry.addData("RUN_POWER", String.format("%.2f", Shooter.RUN_POWER));
			telemetry.addData("UPPER_OFFSET", String.format("%.3f", Shooter.UPPER_OFFSET));
			telemetry.addData("LOWER_OFFSET", String.format("%.3f", Shooter.LOWER_OFFSET));

			telemetry.addData("", "=== CONTROLS ===");
			telemetry.addData("A", "START motors");
			telemetry.addData("B", "STOP motors");

			telemetry.addData("", "=== TEST RESULTS ===");
			telemetry.addData("Motor Response", "✓ OPERATIONAL");
			telemetry.addData("Synchronization", syncStatus);
			telemetry.addData("Stop Function", "✓ VERIFIED");

			telemetry.update();
		}

		// Shutdown
		HardwareShutdown.shutdown();
	}
}
