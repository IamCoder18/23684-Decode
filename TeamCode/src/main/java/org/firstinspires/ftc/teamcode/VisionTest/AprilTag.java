package org.firstinspires.ftc.teamcode.VisionTest;

import android.util.Size;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagGameDatabase;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;

import java.util.List;

@TeleOp(name = "AprilTag", group = "Tests")
public class AprilTag extends LinearOpMode {
	private AprilTagProcessor aprilTag;
	private VisionPortal visionPortal;

	@Override
	public void runOpMode() {
		initAprilTag();

		telemetry.addData("DS preview on/off", "3 dots, Camera Stream");
		telemetry.addData(">", "Touch START to start OpMode");
		telemetry.update();
		waitForStart();

		if (opModeIsActive()) {
			while (opModeIsActive()) {
				telemetryAprilTag();
				telemetry.update();
				sleep(20);
			}
		}
		visionPortal.close();
	}

	private void initAprilTag() {
		aprilTag = new AprilTagProcessor.Builder()
				.setDrawAxes(true)
				.setDrawCubeProjection(true)
				.setDrawTagOutline(true)
				.setTagFamily(AprilTagProcessor.TagFamily.TAG_36h11)
				.setTagLibrary(AprilTagGameDatabase.getDecodeTagLibrary())
				.setOutputUnits(DistanceUnit.INCH, AngleUnit.DEGREES)
				.setLensIntrinsics(1437.0372, 1438.2118, 963.4090, 515.8415)
				.build();

		aprilTag.setDecimation(3);
		VisionPortal.Builder builder = new VisionPortal.Builder();
		builder.setCamera(hardwareMap.get(WebcamName.class, "Webcam 1"));
		builder.setStreamFormat(VisionPortal.StreamFormat.MJPEG);
		builder.setCameraResolution(new Size(1280, 720));
		builder.enableLiveView(true);
		builder.setAutoStartStreamOnBuild(true);
		// Choose whether or not LiveView stops if no processors are enabled.
		// If set "true", monitor shows solid orange screen if no processors enabled.
		// If set "false", monitor shows camera view without annotations.
		builder.setAutoStopLiveView(false);
		builder.addProcessor(aprilTag);
		visionPortal = builder.build();
		visionPortal.setProcessorEnabled(aprilTag, true);
	}


	/**
	 * Add telemetry about AprilTag detections.
	 */
	private void telemetryAprilTag() {

		List<AprilTagDetection> currentDetections = aprilTag.getDetections();
		telemetry.addData("Processor Enabled", visionPortal.getProcessorEnabled(aprilTag));
		telemetry.addData("Current FPS", visionPortal.getFps());
		telemetry.addData("# AprilTags Detected", currentDetections.size());

		// Step through the list of detections and display info for each one.
		for (AprilTagDetection detection : currentDetections) {
			if (detection.metadata != null) {
				telemetry.addLine(String.format("\n==== (ID %d) %s", detection.id, detection.metadata.name));
				telemetry.addLine(String.format("XYZ %6.1f %6.1f %6.1f  (inch)", detection.ftcPose.x, detection.ftcPose.y, detection.ftcPose.z));
				telemetry.addLine(String.format("PRY %6.1f %6.1f %6.1f  (deg)", detection.ftcPose.pitch, detection.ftcPose.roll, detection.ftcPose.yaw));
				telemetry.addLine(String.format("RBE %6.1f %6.1f %6.1f  (inch, deg, deg)", detection.ftcPose.range, detection.ftcPose.bearing, detection.ftcPose.elevation));
			} else {
				telemetry.addLine(String.format("\n==== (ID %d) Unknown", detection.id));
				telemetry.addLine(String.format("Center %6.0f %6.0f   (pixels)", detection.center.x, detection.center.y));
			}
		}   // end for() loop

		// Add "key" information to telemetry
		telemetry.addLine("\nkey:\nXYZ = X (Right), Y (Forward), Z (Up) dist.");
		telemetry.addLine("PRY = Pitch, Roll & Yaw (XYZ Rotation)");
		telemetry.addLine("RBE = Range, Bearing & Elevation");

	}   // end method telemetryAprilTag()

}   // end class

