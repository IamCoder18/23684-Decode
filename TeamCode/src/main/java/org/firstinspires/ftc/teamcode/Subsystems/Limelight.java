package org.firstinspires.ftc.teamcode.Subsystems;

import androidx.annotation.NonNull;

import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.Action;
import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.IMU;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.matrices.VectorF;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose3D;
import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles;
import org.firstinspires.ftc.vision.apriltag.AprilTagGameDatabase;

import java.util.List;


public class Limelight {
	// TODO: Deprecate
	public List position;
	Telemetry telemetry;
	double ObeliskId;
	private Limelight3A limelight;
	private IMU imu;

	private static Limelight instance = null;

	public double Tx;
	public double Ty;
	public double Ta;
	public Pose3D botpose;
	public int aprilTagCount;



	private Limelight() {}

	public static void initialize(HardwareMap hardwareMap) {
		if (instance == null) {
			instance = new Limelight();

			instance.limelight = hardwareMap.get(Limelight3A.class, "limelight");
			instance.limelight.pipelineSwitch(0);
			instance.limelight.start();

			instance.imu = hardwareMap.get(IMU.class, "imu");
			RevHubOrientationOnRobot orientation = new RevHubOrientationOnRobot(
					RevHubOrientationOnRobot.LogoFacingDirection.UP,
					RevHubOrientationOnRobot.UsbFacingDirection.RIGHT
			);
			instance.imu.initialize(new IMU.Parameters(orientation));
			instance.imu.resetYaw();
		}
	}

	public static Limelight getInstance() {
		if (instance == null) {
			throw new IllegalStateException("Limelight not initialized. Call initialize(hardwareMap) first.");
		}
		return instance;
	}

	public double normalizeAngle(double angle) {
		while (angle <= -Math.PI) angle += 2 * Math.PI;
		while (angle > Math.PI) angle -= 2 * Math.PI;
		return angle;
	}

	private void updateValues() {
		limelight.pipelineSwitch(0);

		YawPitchRollAngles angles = imu.getRobotYawPitchRollAngles();

		limelight.updateRobotOrientation(angles.getYaw());
		LLResult llResult = limelight.getLatestResult();

		if (llResult != null && llResult.isValid()) {
			// TODO: Try out MT2
			botpose = llResult.getBotpose();
			Tx = llResult.getTx();
			Ty = llResult.getTy();
			Ta = llResult.getTa();

			aprilTagCount = llResult.getFiducialResults().size();

			List<LLResultTypes.FiducialResult> fiducialResults = llResult.getFiducialResults();
			for (LLResultTypes.FiducialResult fiducial : fiducialResults) {
				int id = fiducial.getFiducialId();

				double distanceFromAprilTag = AprilTagGameDatabase.getDecodeTagLibrary().lookupTag(id).fieldPosition.multiplied(0.0254f).subtracted(new VectorF((float) botpose.getPosition().x, (float) botpose.getPosition().y, 0.7493f)).magnitude();
			}
		}
		telemetry.update();
	}

	public Action update() {
		return new UpdateAction();
	}

	private class UpdateAction implements Action {
		@Override
		public boolean run(@NonNull TelemetryPacket packet) {
			updateValues();
			packet.put("Tx", Tx);
			packet.put("Ty", Ty);
			packet.put("Ta", Ta);
			packet.put("ObeliskId", ObeliskId);
			packet.put("BotPose", botpose);
			packet.put("X", botpose.getPosition().x);
			packet.put("Y", botpose.getPosition().y);
			packet.put("Z", botpose.getPosition().z);
			packet.put("Yaw", normalizeAngle(botpose.getOrientation().getYaw(AngleUnit.DEGREES)));

			return false;
		}
	}

	public List VisionPosition() {
		limelight.pipelineSwitch(0);
		LLResult llResult = limelight.getLatestResult();

		if (llResult != null && llResult.isValid()) {
			Pose3D botPose = llResult.getBotpose();
			position.set(0, botPose.getPosition().x);
			position.set(1, botPose.getPosition().y);
			position.set(2, botPose.getPosition().z);
		}

		return position;
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
}
