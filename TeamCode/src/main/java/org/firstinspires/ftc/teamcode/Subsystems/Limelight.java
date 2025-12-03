package org.firstinspires.ftc.teamcode.Subsystems;

import androidx.annotation.NonNull;

import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.Action;
import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.IMU;

import org.firstinspires.ftc.robotcore.external.matrices.VectorF;
import org.firstinspires.ftc.robotcore.external.navigation.Pose3D;
import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles;
import org.firstinspires.ftc.vision.apriltag.AprilTagGameDatabase;

import java.util.List;

@Config
public class Limelight {

    private static Limelight instance = null;

    private Limelight3A limelight;
    private IMU imu;

    // Latest telemetry data
    public double tx = 0.0;
    public double ty = 0.0;
    public double ta = 0.0;
    public Pose3D botPose = null;
    public YawPitchRollAngles robotOrientation = null;

    private Limelight() {}

    public static void initialize(HardwareMap hardwareMap) {
        if (instance == null) {
            instance = new Limelight();
            instance.limelight = hardwareMap.get(Limelight3A.class, "limelight");
            instance.limelight.pipelineSwitch(0);

            instance.imu = hardwareMap.get(IMU.class, "imu");
            RevHubOrientationOnRobot orientation = new RevHubOrientationOnRobot(
                    RevHubOrientationOnRobot.LogoFacingDirection.UP,
                    RevHubOrientationOnRobot.UsbFacingDirection.RIGHT
            );
            instance.imu.initialize(new IMU.Parameters(orientation));
            instance.imu.resetYaw();

            instance.limelight.start();
        }
    }

    public static Limelight getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Limelight not initialized. Call initialize(hardwareMap) first.");
        }
        return instance;
    }

    public static void shutdown() {
        if (instance != null) {
            // Cleanup if needed
        }
    }

    /**
     * Returns the latest LLResult from the Limelight.
     */
    public LLResult getLatestResult() {
        return limelight.getLatestResult();
    }

    /**
     * Returns the bot pose from the latest result.
     */
    public Pose3D getBotPose() {
        return limelight.getLatestResult().getBotpose_MT2();
    }

    /**
     * Returns the robot orientation angles.
     */
    public YawPitchRollAngles getRobotOrientation() {
        return imu.getRobotYawPitchRollAngles();
    }

    /**
     * Gets the distance to a specific AprilTag by ID.
     */
    public double getDistanceToTag(int tagId) {
        LLResult llResult = limelight.getLatestResult();
		botPose = limelight.getLatestResult().getBotpose();

        if (llResult != null && llResult.isValid() && botPose != null) {
            List<LLResultTypes.FiducialResult> fiducialResults = llResult.getFiducialResults();
            for (LLResultTypes.FiducialResult fiducial : fiducialResults) {
                if (fiducial.getFiducialId() == tagId) {
                    return AprilTagGameDatabase.getDecodeTagLibrary().lookupTag(tagId).fieldPosition.multiplied(0.0254f).subtracted(new VectorF((float) botPose.getPosition().x, (float) botPose.getPosition().y, 0.7493f)).magnitude();
                }
            }
        }
        return -1.0; // Tag not found
    }
}
