package org.firstinspires.ftc.teamcode.Subsystems;
import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.IMU;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.matrices.VectorF;
import org.firstinspires.ftc.robotcore.external.navigation.Pose3D;
import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles;
import org.firstinspires.ftc.vision.apriltag.AprilTagGameDatabase;

import java.util.List;


public class LimeLight {
        private Limelight3A limelight;

       Telemetry telemetry;

        private IMU imu;

        public List position;

        public double normalizeAngle(double angle) {
            while (angle <= -Math.PI) angle += 2 * Math.PI;
            while (angle > Math.PI) angle -= 2 * Math.PI;
            return angle;

        }

        double ObliecksId;



        public LimeLight(HardwareMap hardwareMap) {


            limelight = hardwareMap.get(Limelight3A.class, "limelight");
            limelight.pipelineSwitch(0);
            limelight.start();

            imu = hardwareMap.get(IMU.class, "imu");
            RevHubOrientationOnRobot orientation = new RevHubOrientationOnRobot(
                    RevHubOrientationOnRobot.LogoFacingDirection.UP,
                    RevHubOrientationOnRobot.UsbFacingDirection.RIGHT
            );
            imu.initialize(new IMU.Parameters(orientation));
            imu.resetYaw();
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
                    VectorF target =  AprilTagGameDatabase.getDecodeTagLibrary().lookupTag(id).fieldPosition.multiplied(0.0254f);
                    VectorF robotPose = new VectorF((float) botPose.getPosition().x, (float) botPose.getPosition().y, 0.7493f);
                    VectorF targetDis = target.subtracted(robotPose);
                    telemetry.addLine("Id:" + id + "distance" + targetDis.magnitude());
                    //telemetry.addLine("ID: " + id + " x " + x + " y: " + distance + " z: " + z);
                }


            }

            telemetry.update();
        }


        public String AprilTagId(int pipeline){
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

        public List VisionPosition(){
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

