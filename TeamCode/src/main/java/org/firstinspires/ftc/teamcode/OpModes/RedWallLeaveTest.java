package org.firstinspires.ftc.teamcode.OpModes;

// Added imports
import android.util.Size;
import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.Vector2d;
import com.acmerobotics.roadrunner.ftc.Actions;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.hardware.camera.controls.ExposureControl;
import org.firstinspires.ftc.robotcore.external.hardware.camera.controls.GainControl;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagGameDatabase;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.firstinspires.ftc.teamcode.Utility.Log; // Updated Log import
import org.firstinspires.ftc.teamcode.MecanumDrive;

@Autonomous
public class RedWallLeaveTest extends OpMode {
	Pose2d beginPose;
	MecanumDrive drive;

	// Added AprilTag and Log members
	private AprilTagProcessor aprilTag;
	private VisionPortal visionPortal;
	private Log obeliskLog;

	private int GPPCount = 0;
	private int PGPCount = 0;
	private int PPGCount = 0;

    private static final String LOG_FILE_NAME = "ObeliskDetectionLog"; // Static log file name

	@Override
	public void init() {
		// RoadRunner Init
		beginPose = new Pose2d(66, 15.5, Math.toRadians(180));
		drive = new MecanumDrive(hardwareMap, beginPose);

		// Initialize AprilTag
		initAprilTag();

		// Initialize Logger
		obeliskLog = new Log(LOG_FILE_NAME, false);

		telemetry.addData("Status", "Initialized with AprilTag and Logging");
		telemetry.update();
	}

	private void initAprilTag() {
		aprilTag = new AprilTagProcessor.Builder()
				.setDrawAxes(false) 
				.setDrawCubeProjection(false) 
				.setDrawTagOutline(true)
				.setTagFamily(AprilTagProcessor.TagFamily.TAG_36h11)
				.setTagLibrary(AprilTagGameDatabase.getDecodeTagLibrary())
				.setOutputUnits(DistanceUnit.INCH, AngleUnit.DEGREES)
				.setLensIntrinsics(1437.0372, 1438.2118, 963.4090, 515.8415) // Optional: Add if you have specific calibration
				.build();

		aprilTag.setDecimation(2); 

		VisionPortal.Builder builder = new VisionPortal.Builder();
		builder.setCamera(hardwareMap.get(WebcamName.class, "Webcam 1")); 
		builder.setCameraResolution(new Size(640, 480)); 
		builder.setStreamFormat(VisionPortal.StreamFormat.MJPEG); 
		builder.enableLiveView(true); 
		builder.setAutoStartStreamOnBuild(true);
		builder.setAutoStopLiveView(false); 
		builder.addProcessor(aprilTag);

		visionPortal = builder.build();
		visionPortal.setProcessorEnabled(aprilTag, true);

		while (visionPortal.getCameraState() != VisionPortal.CameraState.STREAMING) {
			telemetry.addLine("Waiting for camera to start streaming");
		}

		ExposureControl exposureControl = visionPortal.getCameraControl(ExposureControl.class);
		GainControl gainControl = visionPortal.getCameraControl(GainControl.class);

		if (exposureControl.getMode() != ExposureControl.Mode.Manual) {
			exposureControl.setMode(ExposureControl.Mode.Manual);
		}
		exposureControl.setExposure(1, TimeUnit.MILLISECONDS);
		gainControl.setGain(200);
        
	}

	@Override
	public void start() {
		Actions.runBlocking(
				drive.actionBuilder(beginPose)
						.splineTo(new Vector2d(36,12), Math.toRadians(180))
						.build());
	}

    private void detectAndLogObelisk() {
        if (aprilTag == null || visionPortal == null || obeliskLog == null) {
            telemetry.addData("Error", "AprilTag or Logger not initialized");
            telemetry.update();
            return;
        }

        List<AprilTagDetection> currentDetections = aprilTag.getDetections();

        if (currentDetections != null && !currentDetections.isEmpty()) {
            telemetry.addData("# AprilTags Detected", currentDetections.size());
            for (AprilTagDetection detection : currentDetections) {
                if (detection.metadata != null) {
                    switch (detection.id) {
                        case 21:
                            GPPCount++;
                            break;
                        case 22:
                            PGPCount++;
                            break;
                        case 23:
                            PPGCount++;
                            break;
                    }
                }
            }
        } else {
            telemetry.addData("# AprilTags Detected", 0);
        }
    }

	@Override
	public void loop() {
		detectAndLogObelisk();

		telemetry.addData("Status", "Running Path / Loop Active");
		telemetry.update();
	}

    @Override
    public void stop() {
        if (obeliskLog != null) {
			String largestPattern = "GPP";
			if (PGPCount > GPPCount) {
				largestPattern = "PGP";
			}
			if (PPGCount > Math.max(GPPCount, PGPCount)) {
				largestPattern = "PPG";
			}

			obeliskLog.addData(largestPattern);
			obeliskLog.update();

			obeliskLog.close();
        }
        if (visionPortal != null) {
            visionPortal.close();
        }
        telemetry.addData("Status", "OpMode Stopped. Log and VisionPortal closed.");
        telemetry.update();
    }
}
