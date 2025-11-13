package org.firstinspires.ftc.teamcode.OpModes.TeleOp;

import static org.firstinspires.ftc.teamcode.Roadrunner.MecanumDrive.PARAMS;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.Action;
import com.acmerobotics.roadrunner.VelConstraint;
import com.acmerobotics.roadrunner.ftc.Actions;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.LifecycleManagementUtilities.HardwareInitializer;
import org.firstinspires.ftc.teamcode.LifecycleManagementUtilities.HardwareShutdown;
import org.firstinspires.ftc.teamcode.Roadrunner.Localizer;
import org.firstinspires.ftc.teamcode.Roadrunner.MecanumDrive;
import org.firstinspires.ftc.teamcode.Roadrunner.PinpointLocalizer;
import org.firstinspires.ftc.teamcode.Subsystems.Intake;
import org.firstinspires.ftc.teamcode.Subsystems.RGBIndicator;
import org.firstinspires.ftc.teamcode.Subsystems.Shooter;
import org.firstinspires.ftc.teamcode.Subsystems.Spindexer;
import org.firstinspires.ftc.teamcode.Subsystems.Transfer;
import org.firstinspires.ftc.teamcode.TrigLocation;
import org.firstinspires.ftc.teamcode.Utilities.ActionScheduler;
import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.PoseVelocity2d;
import com.acmerobotics.roadrunner.Vector2d;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
	protected boolean leftTriggerPressed = false;
	protected boolean rightTriggerPressed = false;
	protected boolean xButtonPressed = false;
	protected boolean spindexerUpCrossed = false;
	protected boolean spindexerMidCrossed = false;
	protected boolean spindexerDownCrossed = false;

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
		scheduler.schedule(Transfer.getInstance().intakeDoorForward());
		scheduler.schedule(Transfer.getInstance().transferBackward());
		scheduler.update();
	}

	@Override
	public void loop() {
		// Update drive with gamepad input
		handleDriveInput();

		//handleDriveInput2();

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

	private void handleDriveInput2(){

		// imports
		Pose2d beginPose;

		//MecanumDrive drive;

		Localizer localizer;

		TrigLocation trig;

		VelConstraint vel;

		int timer = 0;
		FtcDashboard dash = FtcDashboard.getInstance();
		List<Action> runningActions = new ArrayList<>();
		String allianceGoal = "RED GOAL";
		double Dx, Dy,Dh;
		double X,Y,H;
		double aimTrigger;
		double parkTrigger;
		// red park is 37, -33

		beginPose = getStartingPose();

		localizer = new PinpointLocalizer(hardwareMap,PARAMS.inPerTick,beginPose);
		trig = new TrigLocation(drive,localizer,hardwareMap);
		Dx = beginPose.position.x;
		Dy = beginPose.position.y;
		Dh = beginPose.heading.toDouble();

		X = gamepad1.left_stick_y;
		Y = gamepad1.left_stick_x;
		H = gamepad1.right_stick_x;
		aimTrigger = gamepad1.right_trigger;
		parkTrigger = gamepad1.right_trigger;



		telemetry.addData("Status", "Running");
		telemetry.addData("X,Y,H", localizer.getPose());
		telemetry.addLine(allianceGoal);
		telemetry.update();
		localizer.update();
		Pose2d pose = drive.localizer.getPose();

		timer += 1;


		TelemetryPacket packet = new TelemetryPacket();

		// updated based on gamepads

		// update running actions
		List<Action> newActions = new ArrayList<>();
		for (Action action : runningActions) {
			action.preview(packet.fieldOverlay());
			if (action.run(packet)) {
				newActions.add(action);
			}
		}
		runningActions = newActions;

		dash.sendTelemetryPacket(packet);


		if (parkTrigger <= 0.5) {
			if (X != 0) {
				Dx += X * 12.2;
			} else {
				Dx += X + 0.00000001;
			}

			if (Y != 0) {
				Dy += Y * 12.2;
			}  else {
				Dy += Y + 0.00000001;
			}

			if (aimTrigger != 0) {
				if (H != 0) {
					Dh += trig.normalizeAngle(H * Math.toRadians(30));

				}  else {
					Dh += H + 0.00000001;
				}
			} else {
				if (Objects.equals(allianceGoal, "RED GOAL")) {
					Dh = trig.TurnToRed();
				}else if (Objects.equals(allianceGoal, "BLUEGOAL")){
					Dh = trig.TurnToBlue();
				}
			}
		}else{
			Dy = 37;
			Dx = -33;
			Dh = Math.toRadians(90);
		}

		Actions.runBlocking(
				drive.actionBuilder(beginPose)
						.strafeToLinearHeading(new Vector2d(Dx,Dy),Dh)
						.build());


	}

	/**
	 * Handle operator controls from gamepad2
	 */
	protected void handleOperatorInput() {
		// Left Trigger: run intake
		if (gamepad2.left_trigger > 0.5 && !leftTriggerPressed) {
			scheduler.schedule(Intake.getInstance().in());
			leftTriggerPressed = true;
		} else if (gamepad2.left_trigger <= 0.5 && leftTriggerPressed) {
			scheduler.schedule(Intake.getInstance().stop());
			leftTriggerPressed = false;
		}

		// Right Trigger: schedule Shooter.run() once when pressed, Shooter.stop() when released
		if (gamepad2.right_trigger > 0.5 && !rightTriggerPressed) {
			scheduler.schedule(Shooter.getInstance().run());
			scheduler.schedule(Transfer.getInstance().transferForward());
			rightTriggerPressed = true;
		} else if (gamepad2.right_trigger <= 0.5 && rightTriggerPressed) {
			scheduler.schedule(Shooter.getInstance().stop());
			scheduler.schedule(Transfer.getInstance().transferBackward());
			rightTriggerPressed = false;
		}

		// X Button: Transfer forward when pressed, backward when released
		if (gamepad2.x && !xButtonPressed) {
			scheduler.schedule(Transfer.getInstance().transferForward());
			xButtonPressed = true;
		} else if (!gamepad2.x && xButtonPressed) {
			scheduler.schedule(Transfer.getInstance().transferBackward());
			xButtonPressed = false;
		}

		// Left joystick: Spindexer control with threshold crossing
		double leftJoystickY = gamepad2.left_stick_y;
		
		// Dead zone: stop spindexer
		if (leftJoystickY > -0.2 && leftJoystickY < 0.2) {
			if (!spindexerMidCrossed) {
				Spindexer.getInstance().setDirectPower(0);
				spindexerMidCrossed = true;
				spindexerUpCrossed = false;
				spindexerDownCrossed = false;
			}
		}
		// Crosses 0.2 threshold going up (from lower to 0.2+)
		else if (leftJoystickY >= 0.2 && !spindexerUpCrossed) {
			Spindexer.getInstance().setDirectPower(0.5);
			spindexerUpCrossed = true;
			spindexerMidCrossed = false;
			spindexerDownCrossed = false;
		}
		// Crosses -0.2 threshold going down (to -0.2 or below)
		else if (leftJoystickY <= -0.2 && !spindexerDownCrossed) {
			Spindexer.getInstance().setDirectPower(-0.5);
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
		telemetry.addData("Left Joystick Y", String.format("%.2f", gamepad2.left_stick_y));
		telemetry.addData("Spindexer Position", String.format("%.2f rev", Spindexer.getInstance().getCurrentPositionTicks() / Spindexer.TICKS_PER_REV));
		
		telemetry.addData("", "=== SHOOTER ===");
		telemetry.addData("Average RPM", String.format("%.2f", Shooter.getInstance().averageRPM));
	}
}
