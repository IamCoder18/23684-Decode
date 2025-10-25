package org.firstinspires.ftc.teamcode.OpModes;

import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.Vector2d;
import com.acmerobotics.roadrunner.ftc.Actions;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.bylazar.panels.Panels;
import com.bylazar.telemetry.PanelsTelemetry;
import com.bylazar.telemetry.JoinedTelemetry;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.MecanumDrive;
import org.firstinspires.ftc.teamcode.RobotDrawing;

@Autonomous(name = "Blue Goal with Drawing")
public class BlueGoal extends OpMode {
	private Pose2d beginPose;
	private MecanumDrive drive;
	private RobotDrawing drawing;
	private Telemetry joinedTelemetry;

	@Override
	public void init() {
		// Starting pose for Road Runner
		beginPose = new Pose2d(-57.7, -44.5, Math.toRadians(54.046));
		drive = new MecanumDrive(hardwareMap, beginPose);
		Panels.INSTANCE.enable();
		Telemetry panelsFtcTelemetry = PanelsTelemetry.INSTANCE.getFtcTelemetry();
		joinedTelemetry = new JoinedTelemetry(panelsFtcTelemetry, telemetry);

		// Initialize drawing helper
		drawing = new RobotDrawing(18, 17.3);

		telemetry.addData("Status", "Initialized");
		telemetry.update();
	}

	@Override
	public void start() {
		// Run a sequence of Road Runner actions
		Actions.runBlocking(
				drive.actionBuilder(beginPose)
						.strafeToSplineHeading(new Vector2d(-20, -14), Math.toRadians(45))
						.waitSeconds(3)
//						.strafeToSplineHeading(new Vector2d(-10, -30), Math.toRadians(270))
						.splineTo(new Vector2d(-10, -54), Math.toRadians(270))
						.strafeToSplineHeading(new Vector2d(-20, -14), Math.toRadians(45))
						.waitSeconds(3)
//						.strafeToSplineHeading(new Vector2d(14, -30), Math.toRadians(270))
						.splineTo(new Vector2d(14, -54), Math.toRadians(270))
						.strafeToSplineHeading(new Vector2d(-20, -14), Math.toRadians(45))
						.waitSeconds(3)
//						.strafeToSplineHeading(new Vector2d(38, -30), Math.toRadians(270))
						.splineTo(new Vector2d(38, -54), Math.toRadians(270))
						.strafeToSplineHeading(new Vector2d(-20, -14), Math.toRadians(45))
						.build()
		);
	}

	@Override
	public void loop() {
		// Update Road Runner localization
		drive.localizer.update();
		Pose2d currentPose = drive.localizer.getPose();

		// Draw robot on Panels field
		drawing.drawRobot(currentPose);

		// Telemetry feedback
		joinedTelemetry.addData("Position X", currentPose.position.x);
		joinedTelemetry.addData("Position Y", currentPose.position.y);
		joinedTelemetry.addData("Heading (deg)", Math.toDegrees(currentPose.heading.toDouble()));
		joinedTelemetry.update();
	}
}
