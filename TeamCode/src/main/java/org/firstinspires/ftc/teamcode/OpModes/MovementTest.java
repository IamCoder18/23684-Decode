package org.firstinspires.ftc.teamcode.OpModes;

// Added imports

import android.util.Size;

import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.Vector2d;
import com.acmerobotics.roadrunner.ftc.Actions;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.MecanumDrive;
import org.firstinspires.ftc.teamcode.Utility.Log;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagGameDatabase;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;

import java.util.List;

@Autonomous
@Disabled
public class MovementTest extends OpMode {
	Pose2d beginPose;
	MecanumDrive drive;

	@Override
	public void init() {
		// Road Runner Init
		beginPose = new Pose2d(66, 15.5, Math.toRadians(180));
		drive = new MecanumDrive(hardwareMap, beginPose);

		telemetry.addData("Status", "Initialized");
		telemetry.update();
	}

	@Override
	public void start() {
		Actions.runBlocking(
				drive.actionBuilder(beginPose)
						.lineToX(60)
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
