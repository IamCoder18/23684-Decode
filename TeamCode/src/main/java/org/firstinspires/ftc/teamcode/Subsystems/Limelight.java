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
import org.firstinspires.ftc.robotcore.external.navigation.Position;
import org.firstinspires.ftc.teamcode.Utilities.DistanceFromTag;
import org.firstinspires.ftc.vision.apriltag.AprilTagGameDatabase;

import java.lang.reflect.Array;
import java.util.List;

public class Limelight {
	private final Limelight3A limelight;
	private final GoBildaPinpointDriver pinpoint;
	private double yawOffest = 0;
	public Pose3D botPose;
	public boolean validResult;
	public List<LLResultTypes.FiducialResult> tags;
	public List<Integer> tagIds;
	public List<DistanceFromTag> distanceFromTags;

	public Limelight(HardwareMap hardwareMap) {
		limelight = hardwareMap.get(Limelight3A.class, "limelight");
		limelight.pipelineSwitch(0);

		GoBildaPinpointDriver.EncoderDirection yEncoderDirection = GoBildaPinpointDriver.EncoderDirection.REVERSED;
		GoBildaPinpointDriver.EncoderDirection xEncoderDirection = GoBildaPinpointDriver.EncoderDirection.REVERSED;

		pinpoint = hardwareMap.get(GoBildaPinpointDriver.class, "pinpoint");
		pinpoint.setOffsets(-177.8, -63.5, DistanceUnit.MM);
		pinpoint.setEncoderResolution(GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_4_BAR_POD);
		pinpoint.setEncoderDirections(xEncoderDirection, yEncoderDirection);
	}

	public void start() {
		limelight.start();
	}

	public void start(double yawOffest) {
		this.yawOffest = yawOffest;
		limelight.start();
	}

	public void update() {
		limelight.pipelineSwitch(0);
		limelight.updateRobotOrientation(pinpoint.getHeading(AngleUnit.DEGREES) + yawOffest);
		LLResult llResult = limelight.getLatestResult();

		if (llResult != null && llResult.isValid()) {
			Position position = llResult.getBotpose_MT2().getPosition().toUnit(DistanceUnit.INCH);
			botPose = new Pose3D(position, llResult.getBotpose_MT2().getOrientation());
//			validResult = true;
//			tags = llResult.getFiducialResults();
//			tagIds = tags.stream().map(LLResultTypes.FiducialResult::getFiducialId).toList();
//			distanceFromTags = tagIds.stream().map(id -> new DistanceFromTag(id, AprilTagGameDatabase.getDecodeTagLibrary().lookupTag(id).fieldPosition.subtracted(new VectorF((float) position.x, (float) position.y, 29.5f)).magnitude())).toList();
		} else {
			validResult = false;
		}
	}

//	public double DistanceFromGoal() {
//		limelight.pipelineSwitch(0);
//		LLResult llResult = limelight.getLatestResult();
//
//		if (llResult != null && llResult.isValid()) {
//			Pose3D botPose = llResult.getBotpose_MT2();
//			List<LLResultTypes.FiducialResult> fiducialResults = llResult.getFiducialResults();
//			for (LLResultTypes.FiducialResult fiducial : fiducialResults) {
//				int id = fiducial.getFiducialId();
//				VectorF target = AprilTagGameDatabase.getDecodeTagLibrary().lookupTag(id).fieldPosition.multiplied(0.0254f);
//				VectorF robotPose = new VectorF((float) botPose.getPosition().x, (float) botPose.getPosition().y, 0.7493f);
//				VectorF targetDis = target.subtracted(robotPose);
//				return targetDis.magnitude();
//			}
//		}
//
//		return 0;
//	}

//	public boolean AreGoalsFound() {
//		limelight.pipelineSwitch(0);
//		LLResult llResult = limelight.getLatestResult();
//
//		return llResult != null && llResult.isValid();
//	}
}


