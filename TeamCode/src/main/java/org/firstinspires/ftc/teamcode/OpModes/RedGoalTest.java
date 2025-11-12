package org.firstinspires.ftc.teamcode.OpModes;

import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.Vector2d;
import com.acmerobotics.roadrunner.ftc.Actions;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.Roadrunner.MecanumDrive;

@Autonomous
public class RedGoalTest extends OpMode {
	Pose2d beginPose;
	MecanumDrive drive;

	@Override
	public void init() {
		// RoadRunner Init
		beginPose = new Pose2d(-58.45, 44.57, Math.toRadians(-54.046));
		drive = new MecanumDrive(hardwareMap, beginPose);

		telemetry.addData("Status", "Initialized");
		telemetry.update();
	}

	@Override
	public void start() {
		Actions.runBlocking(
				drive.actionBuilder(beginPose)
						.strafeToLinearHeading(new Vector2d(-16, 16), Math.toRadians(-45))
						.strafeToConstantHeading(new Vector2d(0, 24))
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
