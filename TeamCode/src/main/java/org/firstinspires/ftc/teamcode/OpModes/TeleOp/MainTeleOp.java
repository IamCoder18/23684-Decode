package org.firstinspires.ftc.teamcode.OpModes.TeleOp;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.LifecycleManagementUtilities.HardwareInitializer;
import org.firstinspires.ftc.teamcode.LifecycleManagementUtilities.HardwareShutdown;
import org.firstinspires.ftc.teamcode.Roadrunner.MecanumDrive;
import org.firstinspires.ftc.teamcode.Subsystems.Intake;
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
 * - Left Stick: Drive forward/backward
 * - Right Stick: Turn left/right
 * <p>
 * Gamepad 2 (Operator):
 * - A: Intake in
 * - B: Intake out
 * - X: Shooter on
 * - Y: Shooter off
 * - LB: Transfer forward
 * - RB: Transfer stop
 * - DPad Up: Spindexer spin forward
 * - DPad Down: Spindexer spin backward
 * <p>
 * All operations are scheduled through ActionScheduler for consistent execution.
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
	protected boolean spindexerUpPressed = false;
	protected boolean spindexerDownPressed = false;

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
	}

	@Override
	public void loop() {
		// Update drive with gamepad input
		handleDriveInput();

		// Update spindexer PID
		Spindexer.getInstance().update();

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
		if (gamepad2.a && !intakeInPressed) {
			scheduler.schedule(Intake.getInstance().in());
			intakeInPressed = true;
		} else if (!gamepad2.a) {
			intakeInPressed = false;
		}

		if (gamepad2.b && !intakeOutPressed) {
			scheduler.schedule(Intake.getInstance().out());
			intakeOutPressed = true;
		} else if (!gamepad2.b) {
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

		// Spindexer control - continuous spin or hold position
		if (gamepad2.dpad_up && !spindexerUpPressed) {
			Spindexer.getInstance().setTargetPosition(Double.POSITIVE_INFINITY); // Spin forward
			spindexerUpPressed = true;
		} else if (!gamepad2.dpad_up && spindexerUpPressed) {
			// Hold current position when button released
			Spindexer.getInstance().setTargetPosition(Spindexer.getInstance().getCurrentPositionTicks() / Spindexer.TICKS_PER_REV);
			spindexerUpPressed = false;
		}

		if (gamepad2.dpad_down && !spindexerDownPressed) {
			Spindexer.getInstance().setTargetPosition(Double.NEGATIVE_INFINITY); // Spin backward
			spindexerDownPressed = true;
		} else if (!gamepad2.dpad_down && spindexerDownPressed) {
			// Hold current position when button released
			Spindexer.getInstance().setTargetPosition(Spindexer.getInstance().getCurrentPositionTicks() / Spindexer.TICKS_PER_REV);
			spindexerDownPressed = false;
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
		telemetry.addData("DPad UP", "Spindexer Spin Forward");
		telemetry.addData("DPad DOWN", "Spindexer Spin Backward");
		telemetry.addData("Spindexer Position", String.format("%.2f rev", Spindexer.getInstance().getCurrentPositionTicks() / Spindexer.TICKS_PER_REV));
	}
}
