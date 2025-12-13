package org.firstinspires.ftc.teamcode.OpModes.Tests;

import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.Vector2d;
import com.acmerobotics.roadrunner.ftc.Actions;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.Roadrunner.MecanumDrive;

@Autonomous
public class AudienceLeave extends OpMode {
	private static final Pose2d START_POSE = new Pose2d(62.143, -14.717, Math.toRadians(180));
	private static final Vector2d PARK_POSITION = new Vector2d(40, -14);

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
						.strafeTo(PARK_POSITION)
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
