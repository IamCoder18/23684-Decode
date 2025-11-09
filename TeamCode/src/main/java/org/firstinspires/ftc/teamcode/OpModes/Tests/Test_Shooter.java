package org.firstinspires.ftc.teamcode.OpModes.Tests;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.LifecycleManagementUtilities.HardwareInitializer;
import org.firstinspires.ftc.teamcode.LifecycleManagementUtilities.HardwareShutdown;
import org.firstinspires.ftc.teamcode.Subsystems.Shooter;

/**
 * Unit Test OpMode for the Shooter Subsystem
 * <p>
 * Purpose: Tests both shooter motors (upper and lower) for consistent speed and power
 * <p>
 * Controls:
 * - A button: Start both shooter motors
 * - B button: Stop both shooter motors
 * - DPAD UP: Increase RUN_POWER (add 0.1)
 * - DPAD DOWN: Decrease RUN_POWER (subtract 0.1)
 * - DPAD RIGHT: Increase UPPER_OFFSET (add 0.05)
 * - DPAD LEFT: Decrease UPPER_OFFSET (subtract 0.05)
 * - LB/RB buttons: Adjust LOWER_OFFSET (+/- 0.05)
 * <p>
 * Expected Behavior:
 * - RUN button should spin both motors at RUN_POWER speed
 * - STOP button should halt both motors immediately
 * - Offset adjustments allow tuning motor speed differences for synchronization
 * - Telemetry displays current power settings and offset values
 * <p>
 * Notes:
 * - Ensure both motors spin in the same direction when RUN is pressed
 * - Use offsets to compensate for motor speed differences
 * - Monitor for vibration or noise indicating mechanical issues
 */
@TeleOp(name = "Test_Shooter", group = "Unit Tests")
public class Test_Shooter extends LinearOpMode {

	private Shooter shooter;

	@Override
	public void runOpMode() throws InterruptedException {
		// Initialize hardware
		HardwareInitializer.initialize(hardwareMap);
		shooter = Shooter.getInstance();

		telemetry.addData("Status", "Initialized - Waiting for START");
		telemetry.update();

		waitForStart();

		telemetry.addData("Status", "Running");
		telemetry.addData("Controls", "A=RUN, B=STOP");
		telemetry.addData("Tune Power", "DPAD UP/DOWN");
		telemetry.addData("Tune Offsets", "DPAD LEFT/RIGHT, LB/RB");
		telemetry.update();

		while (opModeIsActive()) {
			// A button - Start shooter
			if (gamepad1.a) {
				shooter.run().run(null);
				telemetry.addData("Shooter State", "RUNNING");
			}

			// B button - Stop shooter
			if (gamepad1.b) {
				shooter.stop().run(null);
				telemetry.addData("Shooter State", "STOPPED");
			}

			// DPAD UP - Increase RUN_POWER
			if (gamepad1.dpad_up) {
				Shooter.RUN_POWER = Math.min(Shooter.RUN_POWER + 0.1, 1.0);
				telemetry.addData("RUN_POWER adjusted to", Shooter.RUN_POWER);
				Thread.sleep(200); // Debounce
			}

			// DPAD DOWN - Decrease RUN_POWER
			if (gamepad1.dpad_down) {
				Shooter.RUN_POWER = Math.max(Shooter.RUN_POWER - 0.1, 0.0);
				telemetry.addData("RUN_POWER adjusted to", Shooter.RUN_POWER);
				Thread.sleep(200); // Debounce
			}

			// DPAD RIGHT - Increase UPPER_OFFSET (speed up upper shooter)
			if (gamepad1.dpad_right) {
				Shooter.UPPER_OFFSET = Math.min(Shooter.UPPER_OFFSET + 0.05, 0.5);
				telemetry.addData("UPPER_OFFSET adjusted to", Shooter.UPPER_OFFSET);
				Thread.sleep(200); // Debounce
			}

			// DPAD LEFT - Decrease UPPER_OFFSET (slow down upper shooter)
			if (gamepad1.dpad_left) {
				Shooter.UPPER_OFFSET = Math.max(Shooter.UPPER_OFFSET - 0.05, -0.5);
				telemetry.addData("UPPER_OFFSET adjusted to", Shooter.UPPER_OFFSET);
				Thread.sleep(200); // Debounce
			}

			// Right Bumper - Increase LOWER_OFFSET (speed up lower shooter)
			if (gamepad1.right_bumper) {
				Shooter.LOWER_OFFSET = Math.min(Shooter.LOWER_OFFSET + 0.05, 0.5);
				telemetry.addData("LOWER_OFFSET adjusted to", Shooter.LOWER_OFFSET);
				Thread.sleep(200); // Debounce
			}

			// Left Bumper - Decrease LOWER_OFFSET (slow down lower shooter)
			if (gamepad1.left_bumper) {
				Shooter.LOWER_OFFSET = Math.max(Shooter.LOWER_OFFSET - 0.05, -0.5);
				telemetry.addData("LOWER_OFFSET adjusted to", Shooter.LOWER_OFFSET);
				Thread.sleep(200); // Debounce
			}

			// Display telemetry
			telemetry.addData("RUN_POWER", Shooter.RUN_POWER);
			telemetry.addData("STOP_POWER", Shooter.STOP_POWER);
			telemetry.addData("UPPER_OFFSET", Shooter.UPPER_OFFSET);
			telemetry.addData("LOWER_OFFSET", Shooter.LOWER_OFFSET);
			telemetry.addData("Upper Motor Power", Shooter.RUN_POWER + Shooter.UPPER_OFFSET);
			telemetry.addData("Lower Motor Power", Shooter.RUN_POWER + Shooter.LOWER_OFFSET);
			telemetry.update();
		}

		// Shutdown
		HardwareShutdown.shutdown();
	}
}
