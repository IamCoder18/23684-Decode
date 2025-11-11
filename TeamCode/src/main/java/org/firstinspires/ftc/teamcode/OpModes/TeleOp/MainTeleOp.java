package org.firstinspires.ftc.teamcode.OpModes.TeleOp;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.LifecycleManagementUtilities.HardwareInitializer;
import org.firstinspires.ftc.teamcode.LifecycleManagementUtilities.HardwareShutdown;
import org.firstinspires.ftc.teamcode.Roadrunner.MecanumDrive;
import org.firstinspires.ftc.teamcode.Subsystems.Intake;
import org.firstinspires.ftc.teamcode.Subsystems.RGBIndicator;
import org.firstinspires.ftc.teamcode.Subsystems.Shooter;
import org.firstinspires.ftc.teamcode.Subsystems.Spindexer;
import org.firstinspires.ftc.teamcode.Subsystems.Transfer;
import org.firstinspires.ftc.teamcode.Utilities.ActionScheduler;
import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.PoseVelocity2d;
import com.acmerobotics.roadrunner.Vector2d;

/**
 * Main TeleOp OpMode for driver control
 * <p>
 * Gamepad 1 (Driver):
 * - Left Stick: Drive forward/backward/strafe
 * - Right Stick: Turn left/right
 * <p>
 * Gamepad 2 (Operator):
 * - A: Intake in
 * - B: Intake out
 * - X: Shooter on
 * - Y: Shooter off
 * - LB: Transfer forward
 * - RB: Transfer stop
 * - LT: IntakeDoor open
 * - RT: IntakeDoor close
 * - DPad Up: Spindexer spin forward
 * - DPad Down: Spindexer spin backward
 */
public class MainTeleOp extends OpMode {

	protected MecanumDrive drive;
	protected ActionScheduler scheduler;
	
	// Button state tracking to prevent continuous input
	protected boolean intakeInPressed = false;
	protected boolean intakeOutPressed = false;
	protected boolean shooterOnPressed = false;
	protected boolean shooterOffPressed = false;
	protected boolean transferForwardPressed = false;
	protected boolean transferStopPressed = false;

	@Override
	public void init() {
		// Initialize hardware
		HardwareInitializer.initialize(hardwareMap);
		drive = new MecanumDrive(hardwareMap, getStartingPose());
		scheduler = ActionScheduler.getInstance();

		telemetry.addData("Status", "Initialized - Waiting for START");
		telemetry.update();
	}

	@Override
	public void start() {
		// Called when START is pressed
//		scheduler.schedule(Spindexer.getInstance().zero());
//		scheduler.update();
	}

	@Override
	public void loop() {
		// Update drive with gamepad input
		handleDriveInput();

		// Update spindexer PID
		Spindexer.getInstance().update();

		// Update shooter RPM readings
		Shooter.getInstance().updateRPM();

		// Update RGB indicator based on shooter RPM
		updateRGBIndicator();

		// Update action scheduler
		scheduler.update();

		// Handle operator controls
		handleOperatorInput();

		// Display telemetry
		displayTelemetry();

		telemetry.update();
	}

	@Override
	public void stop() {
		// Called when OpMode is stopped
		HardwareShutdown.shutdown();
	}

	/**
	 * Override this method in subclasses to set the starting pose
	 */
	protected Pose2d getStartingPose() {
		return new Pose2d(0, 0, 0);
	}

	/**
	 * Update RGB indicator color based on shooter RPM.
	 * Red (FF0000) at 0 RPM, Violet (9400D3) at 10000 RPM, interpolated in between.
	 */
	private void updateRGBIndicator() {
		double rpm = Shooter.getInstance().averageRPM;
		double maxRPM = 10000.0;

		// Clamp RPM to 0-maxRPM range
		rpm = Math.max(0, Math.min(maxRPM, rpm));

		// Interpolate between Red (0xFF0000) and Violet (0x9400D3)
		double progress = rpm / maxRPM;

		// Red: (255, 0, 0)
		// Violet: (148, 0, 211)
		int r = (int) (255 + (148 - 255) * progress);
		int g = (int) (0 + (0 - 0) * progress);
		int b = (int) (0 + (211 - 0) * progress);

		// Convert to HEX string
		String hexColor = String.format("%02X%02X%02X", r, g, b);
		RGBIndicator.getInstance().setColor(hexColor);
	}

	/**
	 * Handle driving input from gamepad1
	 */
	private void handleDriveInput() {
		double forwardPower = -gamepad1.left_stick_y; // Left stick Y (inverted)
		double turnPower = gamepad1.right_stick_x;     // Right stick X
		double strafePower = gamepad1.left_stick_x;    // Left stick X

		// Apply deadzone
		forwardPower = Math.abs(forwardPower) > 0.05 ? forwardPower : 0;
		strafePower = Math.abs(strafePower) > 0.05 ? strafePower : 0;
		turnPower = Math.abs(turnPower) > 0.05 ? turnPower : 0;

		// Create velocity command
		PoseVelocity2d velocity = new PoseVelocity2d(
				new Vector2d(forwardPower, strafePower),
				turnPower
		);
		drive.setDrivePowers(velocity);
	}



	/**
	 * Handle operator controls from gamepad2
	 */
	protected void handleOperatorInput() {
		// Intake control
		if (gamepad2.a) {
			if (!intakeInPressed) {
				scheduler.schedule(Intake.getInstance().in());
				intakeInPressed = true;
			}
		} else if (intakeInPressed) {
			scheduler.schedule(Intake.getInstance().stop());
			intakeInPressed = false;
		}

		if (gamepad2.b) {
			if (!intakeOutPressed) {
				scheduler.schedule(Intake.getInstance().out());
				intakeOutPressed = true;
			}
		} else if (intakeOutPressed) {
			scheduler.schedule(Intake.getInstance().stop());
			intakeOutPressed = false;
		}

		// Shooter control
		if (gamepad2.x && !shooterOnPressed) {
			scheduler.schedule(Shooter.getInstance().run());
			shooterOnPressed = true;
		} else if (!gamepad2.x) {
			shooterOnPressed = false;
		}

		if (gamepad2.y && !shooterOffPressed) {
			scheduler.schedule(Shooter.getInstance().stop());
			shooterOffPressed = true;
		} else if (!gamepad2.y) {
			shooterOffPressed = false;
		}

		// Transfer control
		if (gamepad2.left_bumper && !transferForwardPressed) {
			scheduler.schedule(Transfer.getInstance().transferForward());
			transferForwardPressed = true;
		} else if (!gamepad2.left_bumper) {
			transferForwardPressed = false;
		}

		if (gamepad2.right_bumper && !transferStopPressed) {
			scheduler.schedule(Transfer.getInstance().transferStop());
			transferStopPressed = true;
		} else if (!gamepad2.right_bumper) {
			transferStopPressed = false;
		}

		// IntakeDoor control - always runs in
		Transfer.getInstance().setIntakeDoorPower(Transfer.FORWARD_POWER);

		// Spindexer control - direct power with dpad
		if (gamepad2.dpad_up) {
			Spindexer.getInstance().setDirectPower(0.5);
		} else if (gamepad2.dpad_down) {
			Spindexer.getInstance().setDirectPower(-0.3);
		} else {
			Spindexer.getInstance().setDirectPower(0);
		}
	}

	/**
	 * Display telemetry information
	 */
	protected void displayTelemetry() {
		telemetry.addData("", "=== MAIN TELEOP ===");
		telemetry.addData("Drive Mode", "Mecanum");
		
		telemetry.addData("", "=== GAMEPAD 1 (Driver) ===");
		telemetry.addData("Forward", String.format("%.2f", -gamepad1.left_stick_y));
		telemetry.addData("Strafe", String.format("%.2f", gamepad1.left_stick_x));
		telemetry.addData("Turn", String.format("%.2f", gamepad1.right_stick_x));
		
		telemetry.addData("", "=== GAMEPAD 2 (Operator) ===");
		telemetry.addData("A", "Intake IN");
		telemetry.addData("B", "Intake OUT");
		telemetry.addData("X", "Shooter ON");
		telemetry.addData("Y", "Shooter OFF");
		telemetry.addData("LB", "Transfer Forward");
		telemetry.addData("RB", "Transfer Stop");
		telemetry.addData("LT", "IntakeDoor Open");
		telemetry.addData("RT", "IntakeDoor Close");
		telemetry.addData("DPad UP", "Spindexer Spin Forward");
		telemetry.addData("DPad DOWN", "Spindexer Spin Backward");
		telemetry.addData("Spindexer Position", String.format("%.2f rev", Spindexer.getInstance().getCurrentPositionTicks() / Spindexer.TICKS_PER_REV));
		
		telemetry.addData("", "=== SHOOTER ===");
		telemetry.addData("Average RPM", String.format("%.2f", Shooter.getInstance().averageRPM));
	}
}
