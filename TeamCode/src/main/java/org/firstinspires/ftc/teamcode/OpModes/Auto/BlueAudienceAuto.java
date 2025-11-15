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


@Autonomous
public class BlueAudienceAuto extends OpMode {
	public static double shootingX = 57, shootingY = -23; // this is the position used shooting
	public static double Goalx = -60, Goaly = -60; // this is the position of the goal
//	public double topRpM = 2000;
	Spindexer spindexer;
	StupidShooter shooter;
	Pose2d beginPose;
	Transfer transfer;
	Intake intake;
	CRServo transferRight;
	CRServo transferLeft;
	TelemetryPacket telemetryPacket;
	MecanumDrive drive;
	TrajectoryActionBuilder tab1;
	TrajectoryActionBuilder tab2;
	boolean done = false;
	ActionScheduler actionScheduler;

	public static double AngleOfShot(double x, double y) {
		double diff_x = Goalx - x;
		double diff_y = Goaly - y;

		return Math.atan2(-diff_y, -diff_x);
	}

	@Override
	public void init() {
		telemetry.addData("Status", "Initializing subsystems...");
		telemetry.update();

		HardwareInitializer.initialize(hardwareMap);

		shooter = new StupidShooter(hardwareMap);
		telemetry.addData("Subsystem Init", "StupidShooter initialized");
		telemetry.update();

		spindexer = Spindexer.getInstance();
		telemetry.addData("Subsystem Init", "Spindexer initialized");
		telemetry.update();

		transfer = Transfer.getInstance();
		telemetry.addData("Subsystem Init", "Transfer initialized");
		telemetry.update();

		intake = Intake.getInstance();
		telemetry.addData("Subsystem Init", "Intake initialized");
		telemetry.update();

		beginPose = new Pose2d(60, -9, Math.toRadians(0));
		drive = new MecanumDrive(hardwareMap, beginPose);
		telemetry.addData("Subsystem Init", "Drive initialized");
		telemetry.addData("Begin Pose", "X: %.2f, Y: %.2f, Heading: %.2f°", beginPose.position.x, beginPose.position.y, Math.toDegrees(beginPose.heading.toDouble()));
		telemetry.update();

		actionScheduler = ActionScheduler.getInstance();
		telemetry.addData("Subsystem Init", "ActionScheduler initialized");
		telemetry.update();

		tab1 = drive.actionBuilder(new Pose2d(60, -9, Math.toRadians(0)))
				.strafeToLinearHeading(new Vector2d(shootingX, shootingY), AngleOfShot(shootingX, shootingY));
		telemetry.addData("Trajectory", "Tab1 created - Target: (%.1f, %.1f)", shootingX, shootingY);
		telemetry.addData("Trajectory", "Tab1 angle: %.2f°", Math.toDegrees(AngleOfShot(shootingX, shootingY)));
		telemetry.update();

		tab2 = drive.actionBuilder(new Pose2d(shootingX, shootingY, AngleOfShot(shootingX, shootingY)))
				.strafeToLinearHeading(new Vector2d(35, -23), Math.toRadians(270));
		telemetry.addData("Trajectory", "Tab2 created - Target: (35.0, -23.0)");
		telemetry.addData("Trajectory", "Tab2 angle: 270°");
		telemetry.update();

		transferRight = hardwareMap.get(CRServo.class, "transferRight");
		transferLeft = hardwareMap.get(CRServo.class, "transferLeft");

		transferRight.setDirection(DcMotorSimple.Direction.REVERSE);
		telemetry.addData("Subsystem Init", "Transfer servos configured");
		telemetry.addData("Status", "Initialization complete");
		telemetry.update();
	}

	public void start() {
		telemetry.addData("Status", "Match started - scheduling autonomous sequence");
		telemetry.addData("Event", "Action Sequence", "1. Move to shooting position + WindUp");
		telemetry.addData("Event", "Action Sequence", "2. Spindexer + Intake spin");
		telemetry.addData("Event", "Action Sequence", "3. Wait for shooter spike and shoot");
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
						shooter.WaitForSpike(),
						tab2.build()
				)
		);

		telemetry.addData("Status", "Autonomous sequence scheduled and running");
		telemetry.update();
	}

	@Override
	public void loop() {
		// Update action scheduler
		actionScheduler.update();
		shooter.updateRPM();
		drive.updatePoseEstimate();


		if (shooter.averageRPM > Shooter.AUDIENCE_RPM){
			telemetry.addLine("Transfer");
			transferLeft.setPower(1);
			transferRight.setPower(1);
		}else{
			telemetry.addLine("Transfer stop");
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
