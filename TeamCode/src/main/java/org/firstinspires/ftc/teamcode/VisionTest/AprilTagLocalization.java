package org.firstinspires.ftc.teamcode.VisionTest;

import android.util.Size;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.hardware.camera.controls.ExposureControl;
import org.firstinspires.ftc.robotcore.external.hardware.camera.controls.GainControl;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Position;
import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagGameDatabase;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;

import java.util.List;
import java.util.concurrent.TimeUnit;

@TeleOp(name = "AprilTag Localization", group = "Test")
public class AprilTagLocalization extends LinearOpMode {

	private static final boolean USE_WEBCAM = true;  // true for webcam, false for phone camera

	/**
	 * Variables to store the position and orientation of the camera on the robot. Setting these
	 * values requires a definition of the axes of the camera and robot:
	 * <p>
	 * Camera axes:
	 * Origin location: Center of the lens
	 * Axes orientation: +x right, +y down, +z forward (from camera's perspective)
	 * <p>
	 * Robot axes (this is typical, but you can define this however you want):
	 * Origin location: Center of the robot at field height
	 * Axes orientation: +x right, +y forward, +z upward
	 * <p>
	 * Position:
	 * If all values are zero (no translation), that implies the camera is at the center of the
	 * robot. Suppose your camera is positioned 5 inches to the left, 7 inches forward, and 12
	 * inches above the ground - you would need to set the position to (-5, 7, 12).
	 * <p>
	 * Orientation:
	 * If all values are zero (no rotation), that implies the camera is pointing straight up. In
	 * most cases, you'll need to set the pitch to -90 degrees (rotation about the x-axis), meaning
	 * the camera is horizontal. Use a yaw of 0 if the camera is pointing forwards, +90 degrees if
	 * it's pointing straight left, -90 degrees for straight right, etc. You can also set the roll
	 * to +/-90 degrees if it's vertical, or 180 degrees if it's upside-down.
	 */
	private final Position cameraPosition = new Position(DistanceUnit.INCH,
			0, 0, 0, 0);
	private final YawPitchRollAngles cameraOrientation = new YawPitchRollAngles(AngleUnit.DEGREES,
			0, -90, 0, 0);

	/**
	 * The variable to store our instance of the AprilTag processor.
	 */
	private AprilTagProcessor aprilTag;

	/**
	 * The variable to store our instance of the vision portal.
	 */
	private VisionPortal visionPortal;

	@Override
	public void runOpMode() {

		initAprilTag();

		// Wait for the DS start button to be touched.
		telemetry.addData("DS preview on/off", "3 dots, Camera Stream");
		telemetry.addData(">", "Touch START to start OpMode");
		telemetry.update();
		waitForStart();

		while (opModeIsActive()) {

			telemetryAprilTag();

			// Push telemetry to the Driver Station.
			telemetry.update();

			// Save CPU resources; can resume streaming when needed.
			if (gamepad1.dpad_down) {
				visionPortal.stopStreaming();
			} else if (gamepad1.dpad_up) {
				visionPortal.resumeStreaming();
			}

			// Share the CPU.
			sleep(20);
		}

		// Save more CPU resources when camera is no longer needed.
		visionPortal.close();

	}   // end method runOpMode()

	/**
	 * Initialize the AprilTag processor.
	 */
	private void initAprilTag() {

		// Create the AprilTag processor.
		aprilTag = new AprilTagProcessor.Builder()

				// The following default settings are available to un-comment and edit as needed.
				.setDrawAxes(true)
				.setDrawCubeProjection(true)
				.setDrawTagOutline(true)
				.setTagFamily(AprilTagProcessor.TagFamily.TAG_36h11)
				.setTagLibrary(AprilTagGameDatabase.getDecodeTagLibrary())
				.setOutputUnits(DistanceUnit.INCH, AngleUnit.DEGREES)
				.setCameraPose(cameraPosition, cameraOrientation)
				.setLensIntrinsics(1437.0372, 1438.2118, 963.4090, 515.8415)

				.build();

		// Adjust Image Decimation to trade-off detection-range for detection-rate.
		// eg: Some typical detection data using a Logitech C920 WebCam
		// Decimation = 1 ..  Detect 2" Tag from 10 feet away at 10 Frames per second
		// Decimation = 2 ..  Detect 2" Tag from 6  feet away at 22 Frames per second
		// Decimation = 3 ..  Detect 2" Tag from 4  feet away at 30 Frames Per Second (default)
		// Decimation = 3 ..  Detect 5" Tag from 10 feet away at 30 Frames Per Second (default)
		// Note: Decimation can be changed on-the-fly to adapt during a match.
		aprilTag.setDecimation(2);
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
		while (visionPortal.getCameraState() != VisionPortal.CameraState.STREAMING) {
//			telemetry.addLine("Waiting for camera to start streaming");
		}

		ExposureControl exposureControl = visionPortal.getCameraControl(ExposureControl.class);
		GainControl gainControl = visionPortal.getCameraControl(GainControl.class);

		if (exposureControl.getMode() != ExposureControl.Mode.Manual) {
			exposureControl.setMode(ExposureControl.Mode.Manual);
			sleep(50);
		}
		exposureControl.setExposure(1, TimeUnit.MILLISECONDS);
		sleep(20);
		gainControl.setGain(200);
		sleep(20);
//		telemetry.addLine("Current Exposure" + visionPortal.getCameraControl(ExposureControl.class).getExposure(TimeUnit.MILLISECONDS));
//		telemetry.addLine("Current Gain" + visionPortal.getCameraControl(ExposureControl.class).getExposure(TimeUnit.MILLISECONDS));
	}   // end method initAprilTag()

	/**
	 * Add telemetry about AprilTag detections.
	 */
	private void telemetryAprilTag() {

		List<AprilTagDetection> currentDetections = aprilTag.getDetections();
		telemetry.addData("# AprilTags Detected", currentDetections.size());

		// Step through the list of detections and display info for each one.
		for (AprilTagDetection detection : currentDetections) {
			if (detection.metadata != null) {
				telemetry.addLine(String.format("\n==== (ID %d) %s", detection.id, detection.metadata.name));
				// Only use tags that don't have Obelisk in them
				if (!detection.metadata.name.contains("Obelisk")) {
					telemetry.addLine(String.format("XYZ %6.1f %6.1f %6.1f  (inch)",
							detection.robotPose.getPosition().x,
							detection.robotPose.getPosition().y,
							detection.robotPose.getPosition().z));
					telemetry.addLine(String.format("PRY %6.1f %6.1f %6.1f  (deg)",
							detection.robotPose.getOrientation().getPitch(AngleUnit.DEGREES),
							detection.robotPose.getOrientation().getRoll(AngleUnit.DEGREES),
							detection.robotPose.getOrientation().getYaw(AngleUnit.DEGREES)));
				}
			} else {
				telemetry.addLine(String.format("\n==== (ID %d) Unknown", detection.id));
				telemetry.addLine(String.format("Center %6.0f %6.0f   (pixels)", detection.center.x, detection.center.y));
			}
		}   // end for() loop

		// Add "key" information to telemetry
		telemetry.addLine("\nkey:\nXYZ = X (Right), Y (Forward), Z (Up) dist.");
		telemetry.addLine("PRY = Pitch, Roll & Yaw (XYZ Rotation)");

	}   // end method telemetryAprilTag()
}