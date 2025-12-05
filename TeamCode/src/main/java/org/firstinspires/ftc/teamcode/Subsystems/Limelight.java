package org.firstinspires.ftc.teamcode.Subsystems;

import com.acmerobotics.roadrunner.Pose2d;
import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.IMU;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.matrices.VectorF;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose3D;
import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles;
import org.firstinspires.ftc.vision.apriltag.AprilTagGameDatabase;

import java.util.List;


public class Limelight {
	public List position;
	Telemetry telemetry;
	double ObliecksId;
	private final Limelight3A limelight;
	private final GoBildaPinpointDriver pinpoint;
	private final IMU imu;

	public Limelight(HardwareMap hardwareMap) {


		limelight = hardwareMap.get(Limelight3A.class, "limelight");
		limelight.pipelineSwitch(0);


		imu = hardwareMap.get(IMU.class, "imu");
		RevHubOrientationOnRobot orientation = new RevHubOrientationOnRobot(
				RevHubOrientationOnRobot.LogoFacingDirection.UP,
				RevHubOrientationOnRobot.UsbFacingDirection.RIGHT
		);
		imu.initialize(new IMU.Parameters(orientation));
		imu.resetYaw();

		GoBildaPinpointDriver.EncoderDirection yEncoderDirection = GoBildaPinpointDriver.EncoderDirection.REVERSED;
		GoBildaPinpointDriver.EncoderDirection xEncoderDirection = GoBildaPinpointDriver.EncoderDirection.REVERSED;


		pinpoint = hardwareMap.get(GoBildaPinpointDriver.class, "pinpoint");
		pinpoint.setOffsets(-177.8, -63.5, DistanceUnit.MM);
		pinpoint.setEncoderResolution(GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_4_BAR_POD);
		pinpoint.setEncoderDirections(xEncoderDirection, yEncoderDirection);
		pinpoint.resetPosAndIMU();
	}

	public double normalizeAngle(double angle) {
		while (angle <= -Math.PI) angle += 2 * Math.PI;
		while (angle > Math.PI) angle -= 2 * Math.PI;
		return angle;

	}

	public void Start(double initialHeading) {
		limelight.start();
		pinpoint.setHeading(initialHeading, AngleUnit.DEGREES);
	}

	public void UpdateData() {

		YawPitchRollAngles angles = imu.getRobotYawPitchRollAngles();

		limelight.updateRobotOrientation(angles.getYaw());
		LLResult llResult = limelight.getLatestResult();

		if (llResult != null && llResult.isValid()) {
			Pose3D botPose = llResult.getBotpose();
			telemetry.addData("Tx", llResult.getTx());
			telemetry.addData("Ty", llResult.getTy());
			telemetry.addData("Ta", llResult.getTa());
			telemetry.addData("BotPose", botPose.toString());
			telemetry.addData("Orientation", botPose.getOrientation().toString());
			telemetry.addData("imuing", imu.getRobotYawPitchRollAngles());

			List<LLResultTypes.FiducialResult> fiducialResults = llResult.getFiducialResults();
			for (LLResultTypes.FiducialResult fiducial : fiducialResults) {
				int id = fiducial.getFiducialId();
				ObliecksId = fiducial.getFiducialId();
				double distance = fiducial.getRobotPoseTargetSpace().getPosition().y;
				double x = fiducial.getRobotPoseTargetSpace().getPosition().x;
				double z = fiducial.getRobotPoseTargetSpace().getPosition().z;
				VectorF target = AprilTagGameDatabase.getDecodeTagLibrary().lookupTag(id).fieldPosition.multiplied(0.0254f);
				VectorF robotPose = new VectorF((float) botPose.getPosition().x, (float) botPose.getPosition().y, 0.7493f);
				VectorF targetDis = target.subtracted(robotPose);
				telemetry.addLine("Id:" + id + "distance" + targetDis.magnitude());
				//telemetry.addLine("ID: " + id + " x " + x + " y: " + distance + " z: " + z);
			}


		}

		telemetry.update();
	}


	public String AprilTagId(int pipeline) {
		// pipeline 0 is for the goals
		// pipeline 1 is for the artifacts
		limelight.pipelineSwitch(pipeline);
		LLResult llResult = limelight.getLatestResult();

		if (llResult != null && llResult.isValid()) {
			List<LLResultTypes.FiducialResult> fiducialResults = llResult.getFiducialResults();
			for (LLResultTypes.FiducialResult fiducial : fiducialResults) {
				int id = fiducial.getFiducialId();
				return Integer.toString(id);
			}

		}
		return "No Tag Found";

	}

	public Pose2d VisionPose() {
		limelight.pipelineSwitch(0);
		LLResult llResult = limelight.getLatestResult();
		limelight.updateRobotOrientation(pinpoint.getHeading(AngleUnit.DEGREES));
		if (llResult != null && llResult.isValid()) {
			Pose3D botPose = llResult.getBotpose_MT2();

			return new Pose2d(botPose.getPosition().x * 39.3701,
					botPose.getPosition().y * 39.3701,
					Math.toRadians(botPose.getOrientation().getYaw()));
		}
		return new Pose2d(0, 0, 0);
	}


	public double DistanceFromGoal() {
		limelight.pipelineSwitch(0);
		LLResult llResult = limelight.getLatestResult();

		if (llResult != null && llResult.isValid()) {
			Pose3D botPose = llResult.getBotpose();
			List<LLResultTypes.FiducialResult> fiducialResults = llResult.getFiducialResults();
			for (LLResultTypes.FiducialResult fiducial : fiducialResults) {
				int id = fiducial.getFiducialId();
				double distance = fiducial.getRobotPoseTargetSpace().getPosition().y;
				double x = fiducial.getRobotPoseTargetSpace().getPosition().x;
				double z = fiducial.getRobotPoseTargetSpace().getPosition().z;
				VectorF target = AprilTagGameDatabase.getDecodeTagLibrary().lookupTag(id).fieldPosition.multiplied(0.0254f);
				VectorF robotPose = new VectorF((float) botPose.getPosition().x, (float) botPose.getPosition().y, 0.7493f);
				VectorF targetDis = target.subtracted(robotPose);
				telemetry.addLine("Id:" + id + "distance" + targetDis.magnitude());

				return targetDis.magnitude();
			}


		}
		return 0;

	}

	public boolean AreGoalsFound() {
		limelight.pipelineSwitch(0);
		LLResult llResult = limelight.getLatestResult();

		return llResult != null && llResult.isValid();
	}


}


