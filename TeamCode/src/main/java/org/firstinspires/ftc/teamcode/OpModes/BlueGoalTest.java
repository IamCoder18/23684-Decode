package org.firstinspires.ftc.teamcode.OpModes;

import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.Vector2d;
import com.acmerobotics.roadrunner.ftc.Actions;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import org.firstinspires.ftc.teamcode.Roadrunner.MecanumDrive;

@Autonomous
public class BlueGoalTest extends OpMode {
	private static final Pose2d START_POSE = new Pose2d(-58.45, -44.57, Math.toRadians(54.046));
	private static final Vector2d LINEAR_STRAFE_TARGET = new Vector2d(-16, -16);
	private static final double LINEAR_STRAFE_HEADING = Math.toRadians(45);
	private static final Vector2d CONSTANT_STRAFE_TARGET = new Vector2d(0, -24);

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
						.strafeToLinearHeading(LINEAR_STRAFE_TARGET, LINEAR_STRAFE_HEADING)
						.strafeToConstantHeading(CONSTANT_STRAFE_TARGET)
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
