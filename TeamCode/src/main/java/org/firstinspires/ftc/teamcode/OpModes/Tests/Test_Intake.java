package org.firstinspires.ftc.teamcode.OpModes.Tests;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.LifecycleManagementUtilities.HardwareInitializer;
import org.firstinspires.ftc.teamcode.LifecycleManagementUtilities.HardwareShutdown;
import org.firstinspires.ftc.teamcode.Subsystems.Intake;

/**
 * Unit Test OpMode for the Intake Subsystem
 * <p>
 * Purpose: Tests the intake motor's forward, reverse, and stop operations
 * <p>
 * Controls:
 * - A button: Start intake motor IN
 * - B button: Start intake motor OUT
 * - X button: Stop intake motor
 * - DPAD UP: Increase IN_POWER (add 0.1)
 * - DPAD DOWN: Decrease IN_POWER (subtract 0.1)
 * - DPAD RIGHT: Increase OUT_POWER magnitude (subtract 0.1 from -1.0)
 * - DPAD LEFT: Decrease OUT_POWER magnitude (add 0.1 to -1.0)
 * <p>
 * Expected Behavior:
 * - IN button should spin the motor forward at IN_POWER
 * - OUT button should spin the motor backward at OUT_POWER
 * - STOP button should stop the motor immediately
 * - Power adjustments via DPAD allow fine-tuning motor behavior
 */
@TeleOp(name = "Test_Intake", group = "Unit Tests")
public class Test_Intake extends LinearOpMode {

	private Intake intake;

	@Override
	public void runOpMode() throws InterruptedException {
		// Initialize hardware
		HardwareInitializer.initialize(hardwareMap);
		intake = Intake.getInstance();

		telemetry.addData("Status", "Initialized - Waiting for START");
		telemetry.update();

		waitForStart();

		telemetry.addData("Status", "Running");
		telemetry.addData("Controls", "A=IN, B=OUT, X=STOP");
		telemetry.addData("Fine Tune", "DPAD UP/DOWN/LEFT/RIGHT");
		telemetry.update();

		while (opModeIsActive()) {
			// A button - Run intake IN
			if (gamepad1.a) {
				intake.in().run(null);
				telemetry.addData("Motor State", "RUNNING IN");
			}

			// B button - Run intake OUT
			if (gamepad1.b) {
				intake.out().run(null);
				telemetry.addData("Motor State", "RUNNING OUT");
			}

			// X button - Stop intake
			if (gamepad1.x) {
				intake.stop().run(null);
				telemetry.addData("Motor State", "STOPPED");
			}

			// DPAD UP - Increase IN_POWER
			if (gamepad1.dpad_up) {
				Intake.IN_POWER = Math.min(Intake.IN_POWER + 0.1, 1.0);
				telemetry.addData("IN_POWER adjusted to", Intake.IN_POWER);
				Thread.sleep(200); // Debounce
			}

			// DPAD DOWN - Decrease IN_POWER
			if (gamepad1.dpad_down) {
				Intake.IN_POWER = Math.max(Intake.IN_POWER - 0.1, 0.0);
				telemetry.addData("IN_POWER adjusted to", Intake.IN_POWER);
				Thread.sleep(200); // Debounce
			}

			// DPAD RIGHT - Increase OUT_POWER magnitude (make more negative)
			if (gamepad1.dpad_right) {
				Intake.OUT_POWER = Math.max(Intake.OUT_POWER - 0.1, -1.0);
				telemetry.addData("OUT_POWER adjusted to", Intake.OUT_POWER);
				Thread.sleep(200); // Debounce
			}

			// DPAD LEFT - Decrease OUT_POWER magnitude (make less negative)
			if (gamepad1.dpad_left) {
				Intake.OUT_POWER = Math.min(Intake.OUT_POWER + 0.1, 0.0);
				telemetry.addData("OUT_POWER adjusted to", Intake.OUT_POWER);
				Thread.sleep(200); // Debounce
			}

			// Display telemetry
			telemetry.addData("IN_POWER", Intake.IN_POWER);
			telemetry.addData("OUT_POWER", Intake.OUT_POWER);
			telemetry.addData("STOP_POWER", Intake.STOP_POWER);
			telemetry.update();
		}

		// Shutdown
		HardwareShutdown.shutdown();
	}
}
