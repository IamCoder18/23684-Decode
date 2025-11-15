package org.firstinspires.ftc.teamcode.OpModes.TeleOp;

import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.PoseVelocity2d;
import com.acmerobotics.roadrunner.Vector2d;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.LifecycleManagementUtilities.HardwareInitializer;
import org.firstinspires.ftc.teamcode.LifecycleManagementUtilities.HardwareShutdown;
import org.firstinspires.ftc.teamcode.Roadrunner.MecanumDrive;
import org.firstinspires.ftc.teamcode.Subsystems.Intake;
import org.firstinspires.ftc.teamcode.Subsystems.RGBIndicator;
import org.firstinspires.ftc.teamcode.Subsystems.Shooter;
import org.firstinspires.ftc.teamcode.Subsystems.Spindexer;
import org.firstinspires.ftc.teamcode.Subsystems.Transfer;
import org.firstinspires.ftc.teamcode.Utilities.ActionScheduler;

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
	protected Shooter shooter;
	protected Intake intake;
	protected Transfer transfer;
	protected Spindexer spindexer;
	protected RGBIndicator rgbIndicator;

	// Button state tracking to prevent continuous input
	protected boolean leftTriggerPressed = false;
	protected boolean rightTriggerPressed = false;
	protected boolean xButtonPressed = false;
	protected boolean bButtonPressed = false;
	protected boolean spindexerUpCrossed = false;
	protected boolean spindexerMidCrossed = false;
	protected boolean spindexerDownCrossed = false;
	protected boolean transferAboveRPM = false;

	@Override
	public void init() {
		// Initialize hardware
		HardwareInitializer.initialize(hardwareMap);
		drive = new MecanumDrive(hardwareMap, getStartingPose());
		scheduler = ActionScheduler.getInstance();
		shooter = Shooter.getInstance();
		intake = Intake.getInstance();
		transfer = Transfer.getInstance();
		spindexer = Spindexer.getInstance();
		rgbIndicator = RGBIndicator.getInstance();

		telemetry.addData("Status", "Initialized - Waiting for START");
		telemetry.update();
	}

	@Override
	public void start() {
		// Called when START is pressed
		scheduler.schedule(transfer.intakeDoorForward());
		scheduler.schedule(transfer.transferBackward());
		scheduler.update();
	}

	@Override
	public void loop() {
		// Update drive with gamepad input
		handleDriveInput();

		// Update spindexer PID
		spindexer.update();

		// Update shooter RPM readings
		shooter.updateRPM();

		// Handle operator controls (must be before scheduler.update())
		handleOperatorInput();

		// Update action scheduler
		scheduler.update();

		// Update RGB indicator based on shooter RPM (after scheduler.update())
		updateRGBIndicator();

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
	 * Update RGB indicator color based on shooter RPM using discrete color ranges.
	 * Predefined colors: Off, Red, Orange, Yellow, Sage, Green, Azure, Blue, Indigo, Violet, White
	 */
	private void updateRGBIndicator() {
		double rpm = shooter.averageRPM;
		double maxRPM = 3000.0;

		// Clamp RPM to 0-maxRPM range
		rpm = Math.max(0, Math.min(maxRPM, rpm));

		// Discrete RPM ranges (0-maxRPM) mapped to 11 color positions
		String[] colorNames = {"OFF", "RED", "ORANGE", "YELLOW", "SAGE", "GREEN", "AZURE", "BLUE", "INDIGO", "VIOLET", "WHITE"};

		// Determine which color range the current RPM falls into
		int colorIndex = (int) (rpm / maxRPM * (colorNames.length - 1));
		colorIndex = Math.min(colorIndex, colorNames.length - 1);

		// Set the servo to the corresponding discrete position
		rgbIndicator.setColorByName(colorNames[colorIndex]);
	}

	/**
	 * Handle driving input from gamepad1
	 */
	private void handleDriveInput() {
		double forwardPower = -gamepad1.left_stick_y; // Left stick Y (inverted)
		double turnPower = -gamepad1.right_stick_x;     // Right stick X
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
		double rpm = shooter.averageRPM;
		double topRPM = 2500;

		// Left Trigger: run intake
		if (gamepad2.left_trigger > 0.5 && !leftTriggerPressed) {
			scheduler.schedule(intake.in());
			leftTriggerPressed = true;
		} else if (gamepad2.left_trigger <= 0.5 && leftTriggerPressed) {
			scheduler.schedule(intake.stop());
			leftTriggerPressed = false;
		}

		// Right Trigger: schedule Shooter.run() once when pressed, Shooter.stop() when released
		if (gamepad2.right_trigger > 0.5 && !rightTriggerPressed) {
			scheduler.schedule(shooter.run(Shooter.AUDIENCE_RPM));
			rightTriggerPressed = true;
		} else if (gamepad2.right_trigger <= 0.5 && rightTriggerPressed) {
			scheduler.schedule(shooter.stop());
			rightTriggerPressed = false;
		}

		// X Button: Override transfer - manual control
		if (gamepad2.x && !xButtonPressed) {
			scheduler.schedule(transfer.transferForward());
			xButtonPressed = true;
		} else if (!gamepad2.x && xButtonPressed) {
			xButtonPressed = false;
		}
		
		// Automatic transfer based on RPM (only if X button not held)
		if (!gamepad2.x) {
			boolean isAboveRPM = rpm >= topRPM;
			if (isAboveRPM && !transferAboveRPM) {
				scheduler.schedule(transfer.transferForward());
			} else if (!isAboveRPM && transferAboveRPM) {
				scheduler.schedule(transfer.transferBackward());
			}
			transferAboveRPM = isAboveRPM;
		}

		// B Button: Intake door backward and intake out when pressed, forward and intake in when released
		if (gamepad2.b && !bButtonPressed) {
			scheduler.schedule(transfer.intakeDoorBackward());
			scheduler.schedule(intake.out());
			bButtonPressed = true;
		} else if (!gamepad2.b && bButtonPressed) {
			scheduler.schedule(transfer.intakeDoorForward());
			scheduler.schedule(intake.stop());
			bButtonPressed = false;
		}

		// Left joystick: Spindexer control with threshold crossing (inverted Y axis)
		double leftJoystickY = -gamepad2.left_stick_y;

		// Dead zone: stop spindexer
		if (leftJoystickY > -0.2 && leftJoystickY < 0.2) {
			if (!spindexerMidCrossed) {
				scheduler.schedule(spindexer.setDirectPower(0));
				spindexerMidCrossed = true;
				spindexerUpCrossed = false;
				spindexerDownCrossed = false;
			}
		}

		// Crosses 0.2 threshold going up (from lower to 0.2+)
		else if (leftJoystickY >= 0.2 && !spindexerUpCrossed) {
			scheduler.schedule(spindexer.setDirectPower(0.25));
			spindexerUpCrossed = true;
			spindexerMidCrossed = false;
			spindexerDownCrossed = false;
		}

		// Crosses -0.2 threshold going down (to -0.2 or below)
		else if (leftJoystickY <= -0.2 && !spindexerDownCrossed) {
			scheduler.schedule(spindexer.setDirectPower(-0.25));
			spindexerDownCrossed = true;
			spindexerMidCrossed = false;
			spindexerUpCrossed = false;
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
		telemetry.addData("Left Trigger", "Intake");
		telemetry.addData("Right Trigger", "Shooter");
		telemetry.addData("Left Joystick Y (Spindexer)", String.format("%.2f", -gamepad2.left_stick_y));
		telemetry.addData("Spindexer Position", String.format("%.2f rev", spindexer.getCurrentPositionTicks() / Spindexer.TICKS_PER_REV));

		telemetry.addData("", "=== SHOOTER ===");
		telemetry.addData("Upper RPM", String.format("%.2f", shooter.upperRPM));
		telemetry.addData("Lower RPM", String.format("%.2f", shooter.lowerRPM));
		telemetry.addData("Average RPM", String.format("%.2f", shooter.averageRPM));
	}
}
