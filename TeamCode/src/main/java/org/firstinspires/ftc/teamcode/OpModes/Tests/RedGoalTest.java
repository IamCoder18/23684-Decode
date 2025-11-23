package org.firstinspires.ftc.teamcode.OpModes.Tests;

import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.Vector2d;
import com.acmerobotics.roadrunner.ftc.Actions;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.Roadrunner.MecanumDrive;

@Autonomous
public class RedGoalTest extends OpMode {
	private static final Pose2d START_POSE = new Pose2d(-58.45, 44.57, Math.toRadians(-54.046));
	private static final Vector2d STRAFE_TARGET_1 = new Vector2d(-16, 16);
	private static final double STRAFE_HEADING_1 = Math.toRadians(-45);
	private static final Vector2d STRAFE_TARGET_2 = new Vector2d(0, 24);

	private Pose2d beginPose;
	private MecanumDrive drive;

	@Override
	public void init() {
		// RoadRunner Init
		beginPose = START_POSE;
		drive = new MecanumDrive(hardwareMap, beginPose);

		telemetry.addData("Status", "Initialized");
		telemetry.update();
	}

	@Override
	public void start() {
		Actions.runBlocking(
				drive.actionBuilder(beginPose)
						.strafeToLinearHeading(STRAFE_TARGET_1, STRAFE_HEADING_1)
						.strafeToConstantHeading(STRAFE_TARGET_2)
						.build());
	}

	@Override
	public void loop() {
		telemetry.addData("Status", "Running Path / Loop Active");
		telemetry.update();
	}

	@Override
	public void stop() {
		telemetry.addData("Status", "OpMode Stopped.");
		telemetry.update();
	}
}
