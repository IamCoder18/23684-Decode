package org.firstinspires.ftc.teamcode.OpModes.Auto;

import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.ParallelAction;
import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.SequentialAction;
import com.acmerobotics.roadrunner.TrajectoryActionBuilder;
import com.acmerobotics.roadrunner.Vector2d;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

import org.firstinspires.ftc.teamcode.LifecycleManagementUtilities.HardwareInitializer;
import org.firstinspires.ftc.teamcode.Roadrunner.MecanumDrive;
import org.firstinspires.ftc.teamcode.Subsystems.Intake;
import org.firstinspires.ftc.teamcode.Subsystems.Shooter;
import org.firstinspires.ftc.teamcode.Subsystems.Spindexer;
import org.firstinspires.ftc.teamcode.Subsystems.StupidShooter;
import org.firstinspires.ftc.teamcode.Subsystems.Transfer;
import org.firstinspires.ftc.teamcode.Utilities.ActionScheduler;
import org.firstinspires.ftc.teamcode.Utilities.GoalAngleCalculator;

@Autonomous
public class BlueAudienceAuto extends OpMode {

	// ============== Constants ==============
	public static double shootingX = 57, shootingY = -23;

	// ============== Member Variables ==============
	// Subsystems
	private Spindexer spindexer;
	private StupidShooter shooter;
	private Transfer transfer;
	private Intake intake;

	// Hardware
	private CRServo transferRight;
	private CRServo transferLeft;

	// Drive and Trajectory
	private MecanumDrive drive;
	private Pose2d beginPose;
	private TrajectoryActionBuilder tab1;
	private TrajectoryActionBuilder tab2;

	// Scheduler
	private ActionScheduler actionScheduler;
	private boolean done = false;

	// ============== Lifecycle Methods ==============
	@Override
	public void init() {
		telemetry.addData("Status", "Initializing subsystems...");
		telemetry.update();

		HardwareInitializer.initialize(hardwareMap);

		// Initialize shooter
		shooter = new StupidShooter(hardwareMap);
		telemetry.addData("Subsystem Init", "StupidShooter initialized");
		telemetry.update();

		// Initialize spindexer
		spindexer = Spindexer.getInstance();
		telemetry.addData("Subsystem Init", "Spindexer initialized");
		telemetry.update();

		// Initialize transfer
		transfer = Transfer.getInstance();
		telemetry.addData("Subsystem Init", "Transfer initialized");
		telemetry.update();

		// Initialize intake
		intake = Intake.getInstance();

		// Initialize transfer servos
		transferLeft = hardwareMap.get(CRServo.class, "transferLeft");
		transferRight = hardwareMap.get(CRServo.class, "transferRight");
		transferRight.setDirection(DcMotorSimple.Direction.REVERSE);

		telemetry.addData("Subsystem Init", "Intake initialized");
		telemetry.update();

		// Initialize drive
		beginPose = new Pose2d(60, -9, Math.toRadians(0));
		drive = new MecanumDrive(hardwareMap, beginPose);
		telemetry.addData("Subsystem Init", "Drive initialized");
		telemetry.addData("Begin Pose", "X: %.2f, Y: %.2f, Heading: %.2f°",
				beginPose.position.x, beginPose.position.y, Math.toDegrees(beginPose.heading.toDouble()));
		telemetry.update();

		// Initialize action scheduler
		actionScheduler = ActionScheduler.getInstance();
		telemetry.addData("Subsystem Init", "ActionScheduler initialized");
		telemetry.update();

		// Build trajectories
		tab1 = drive.actionBuilder(new Pose2d(54, -9, Math.toRadians(0)))
				.strafeToLinearHeading(new Vector2d(shootingX, shootingY), GoalAngleCalculator.calculateAngle(shootingX, shootingY));
		telemetry.addData("Trajectory", "Tab1 created", shootingX, shootingY);
		telemetry.addData("Trajectory", "Tab1 angle: %.2f°", Math.toDegrees(GoalAngleCalculator.calculateAngle(shootingX, shootingY)));
		telemetry.update();

		tab2 = drive.actionBuilder(new Pose2d(shootingX, shootingY, GoalAngleCalculator.calculateAngle(shootingX, shootingY)))
				.strafeToLinearHeading(new Vector2d(35, -23), Math.toRadians(270));
		telemetry.addData("Trajectory", "Tab2 created");
		telemetry.update();

		telemetry.addData("Status", "Initialization complete");
		telemetry.update();
	}

	@Override
	public void start() {
		telemetry.addData("Status", "Match started - scheduling autonomous sequence");
		telemetry.addData("Event", "Action Sequence", "1. Move to shooting position + WindUp");
		telemetry.addData("Event", "Action Sequence", "2. Spindexer + Intake spin");
		telemetry.addData("Event", "Action Sequence", "3. Wait for shooter to shoot");
		telemetry.addData("Event", "Action Sequence", "4. Repeat spin cycles");
		telemetry.addData("Event", "Action Sequence", "5. Move to collection position");
		telemetry.update();

		actionScheduler.schedule(
				new SequentialAction(
						new ParallelAction(
								tab1.build(),
								shooter.WindUp()
						),
						new ParallelAction(
								spindexer.setDirectPower(0.8),
								transfer.intakeDoorForward()
						),
						shooter.WaitForSpike(),
						shooter.WindUp(),
						new ParallelAction(
								spindexer.setDirectPower(0.8),
								transfer.intakeDoorForward()
						),
						shooter.WaitForSpike(),
						new ParallelAction(
								shooter.WindUp()
						),
						new ParallelAction(
								spindexer.setDirectPower(0.8),
								transfer.intakeDoorForward()
						),
						shooter.WaitForSpike()
						// TODO: Uncomment when needed
						//tab2.build(),
						//spindexer.setDirectPower(0),
						//transfer.intakeDoorStop(),
						//shooter.Stop()
				)
		);

		telemetry.addData("Status", "Autonomous sequence scheduled and running");
		telemetry.update();
	}

	@Override
	public void loop() {
		// Update systems
		actionScheduler.update();
		shooter.updateRPM();
		drive.updatePoseEstimate();

		// Control transfer servos based on shooter RPM TODO: TEST WITH TRANSFER SUBSYSTEM INSTEAD OF DIRECTLY
		if (shooter.averageRPM > (Shooter.AUDIENCE_RPM - 400)) {
			transferLeft.setPower(1);
			transferRight.setPower(1);
		} else {
			transferLeft.setPower(0);
			transferRight.setPower(0);
		}

		// Telemetry - Drive position
		Pose2d currentPose = drive.localizer.getPose();
		telemetry.addData("Drive Status", "Current Position");
		telemetry.addData("Position X", "%.2f", currentPose.position.x);
		telemetry.addData("Position Y", "%.2f", currentPose.position.y);
		telemetry.addData("Heading", "%.2f°", Math.toDegrees(currentPose.heading.toDouble()));

		// Telemetry - Action scheduler status
		telemetry.addData("Scheduler Status", !actionScheduler.isSchedulerEmpty() ? "Busy" : "Idle");

		// Telemetry - Shooter status
		telemetry.addData("Shooter RPM", "%.0f", shooter.averageRPM);
		telemetry.addData("Shooter Target RPM", "%.0f", Shooter.AUDIENCE_RPM);

		telemetry.update();
	}
}
