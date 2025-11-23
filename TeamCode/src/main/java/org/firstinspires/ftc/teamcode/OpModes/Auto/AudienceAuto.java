package org.firstinspires.ftc.teamcode.OpModes.Auto;

import androidx.annotation.NonNull;

import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.Action;
import com.acmerobotics.roadrunner.ParallelAction;
import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.SequentialAction;
import com.acmerobotics.roadrunner.TrajectoryActionBuilder;
import com.acmerobotics.roadrunner.Vector2d;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

import org.firstinspires.ftc.teamcode.LifecycleManagementUtilities.HardwareInitializer;
import org.firstinspires.ftc.teamcode.Roadrunner.MecanumDrive;
import org.firstinspires.ftc.teamcode.Subsystems.Intake;
import org.firstinspires.ftc.teamcode.Subsystems.Shooter;
import org.firstinspires.ftc.teamcode.Subsystems.Spindexer;
import org.firstinspires.ftc.teamcode.Subsystems.Transfer;
import org.firstinspires.ftc.teamcode.Utilities.ActionScheduler;
import org.firstinspires.ftc.teamcode.Utilities.DeadlineAction;

/**
 * Abstract base class for Audience-side autonomous modes.
 * Subclasses should implement alliance-specific constants and pose methods.
 */
public abstract class AudienceAuto extends OpMode {
	// Alliance-specific constants (implemented by subclasses)
	protected abstract double getShootingX();
	protected abstract double getShootingY();
	protected abstract double getGoalX();
	protected abstract double getGoalY();
	protected abstract Pose2d getStartPose();
	protected abstract Pose2d getInitialTrajectoryStartPose();
	protected abstract Vector2d getCollectionPosition();
	protected abstract double getCollectionHeading();

	private Spindexer spindexer;
	private Shooter shooter;
	private Transfer transfer;
	private Intake intake;
	private CRServo transferRight;
	private CRServo transferLeft;
	private MecanumDrive drive;
	private TrajectoryActionBuilder trajectoryToShootingPosition;
	private TrajectoryActionBuilder trajectoryToCollectionPosition;
	private ActionScheduler actionScheduler;

	protected double calculateShotAngle(double x, double y) {
		double deltaX = getGoalX() - x;
		double deltaY = getGoalY() - y;
		return Math.atan2(-deltaY, -deltaX);
	}

	@Override
	public void init() {
		telemetry.addData("Status", "Initializing subsystems...");
		telemetry.update();

		HardwareInitializer.initialize(hardwareMap);

		Shooter.initialize(hardwareMap);
		shooter = Shooter.getInstance();
		telemetry.addData("Subsystem Init", "Shooter initialized");
		telemetry.update();

		spindexer = Spindexer.getInstance();
		telemetry.addData("Subsystem Init", "Spindexer initialized");
		telemetry.update();

		transfer = Transfer.getInstance();
		telemetry.addData("Subsystem Init", "Transfer initialized");
		telemetry.update();

		intake = Intake.getInstance();

		transferLeft = hardwareMap.get(CRServo.class, "transferLeft");
		transferRight = hardwareMap.get(CRServo.class, "transferRight");
		transferRight.setDirection(DcMotorSimple.Direction.REVERSE);

		telemetry.addData("Subsystem Init", "Intake initialized");
		telemetry.update();

		Pose2d startPose = getStartPose();
		drive = new MecanumDrive(hardwareMap, startPose);
		telemetry.addData("Subsystem Init", "Drive initialized");
		telemetry.addData("Start Pose", "X: %.2f, Y: %.2f, Heading: %.2f°", startPose.position.x, startPose.position.y, Math.toDegrees(startPose.heading.toDouble()));
		telemetry.update();

		actionScheduler = ActionScheduler.getInstance();
		telemetry.addData("Subsystem Init", "ActionScheduler initialized");
		telemetry.update();

		double shotAngle = calculateShotAngle(getShootingX(), getShootingY());
		trajectoryToShootingPosition = drive.actionBuilder(getInitialTrajectoryStartPose())
				.strafeToLinearHeading(new Vector2d(getShootingX(), getShootingY()), shotAngle);
		telemetry.addData("Trajectory", "To Shooting Position - Target: (%.1f, %.1f)", getShootingX(), getShootingY());
		telemetry.addData("Trajectory", "Shot angle: %.2f°", Math.toDegrees(shotAngle));
		telemetry.update();

		trajectoryToCollectionPosition = drive.actionBuilder(new Pose2d(getShootingX(), getShootingY(), shotAngle))
				.strafeToLinearHeading(getCollectionPosition(), Math.toRadians(getCollectionHeading()));
		telemetry.addData("Trajectory", "To Collection Position - Target: (%.1f, %.1f)", getCollectionPosition().x, getCollectionPosition().y);
		telemetry.addData("Trajectory", "Collection angle: %.2f°", getCollectionHeading());
		telemetry.update();

		telemetry.addData("Status", "Initialization complete");
		telemetry.update();
	}

	@Override
	public void start() {
		telemetry.addData("Status", "Match started - scheduling autonomous sequence");
		telemetry.addData("Event", "Action Sequence", "1. Move to shooting position + spin up");
		telemetry.addData("Event", "Action Sequence", "2. Spindexer + Intake spin");
		telemetry.addData("Event", "Action Sequence", "3. Maintain shooter RPM and fire");
		telemetry.addData("Event", "Action Sequence", "4. Repeat spin cycles");
		telemetry.addData("Event", "Action Sequence", "5. Move to collection position");
		telemetry.update();

		actionScheduler.schedule(
				new SequentialAction(
						new ParallelAction(
								trajectoryToShootingPosition.build(),
								shooter.run(Shooter.AUDIENCE_RPM)
						),
						new DeadlineAction(
								spindexer.setDirectPower(0.8),
								transfer.intakeDoorForward(),
								shooter.run(Shooter.AUDIENCE_RPM),
								new Action() {
									boolean initialized = false;
									boolean wasBelow = false;
									int count = 0;

									@Override
									public boolean run(@NonNull TelemetryPacket packet) {
										// Get current reading
										double currentRPM = shooter.averageRPM;
										boolean isBelow = currentRPM < Shooter.AUDIENCE_SHOT_RPM;

										// Initialize state on the first run
										if (!initialized) {
											wasBelow = isBelow;
											initialized = true;
										}

										// Check for the "Rising Edge" (transition from Under to Above)
										if (wasBelow && !isBelow) {
											count++;
										}

										// Update state for the next loop
										wasBelow = isBelow;

										// Send telemetry for debugging (optional)
										packet.put("RPM", currentRPM);
										packet.put("Cycles", count);

										// Return true to keep running, false to stop after 3rd completion
										return count < 3;
									}
								}
						),
						shooter.run(0),
						spindexer.setDirectPower(0),
						transfer.intakeDoorStop(),
						trajectoryToCollectionPosition.build(),
						shooter.stop()
				)
		);

		telemetry.addData("Status", "Autonomous sequence scheduled and running");
		telemetry.update();
	}

	@Override
	public void loop() {
		actionScheduler.update();
		shooter.updateRPM(System.nanoTime());
		drive.updatePoseEstimate();

		// Control transfer mechanism based on shooter RPM
		if (shooter.isAtTargetRPM(Shooter.AUDIENCE_RPM)) {
			transferLeft.setPower(1);
			transferRight.setPower(1);
		} else {
			transferLeft.setPower(0);
			transferRight.setPower(0);
		}

		// Update telemetry
		Pose2d currentPose = drive.localizer.getPose();
		telemetry.addData("Drive Position X", "%.2f", currentPose.position.x);
		telemetry.addData("Drive Position Y", "%.2f", currentPose.position.y);
		telemetry.addData("Drive Heading", "%.2f°", Math.toDegrees(currentPose.heading.toDouble()));
		telemetry.addData("Scheduler Status", !actionScheduler.isSchedulerEmpty() ? "Busy" : "Idle");
		telemetry.addData("Shooter RPM", "%.0f", shooter.averageRPM);
		telemetry.addData("Shooter Target RPM", "%.0f", Shooter.AUDIENCE_RPM);
		telemetry.update();
	}
}
