package org.firstinspires.ftc.teamcode;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.IMU;

import org.firstinspires.ftc.robotcore.external.matrices.VectorF;
import org.firstinspires.ftc.robotcore.external.navigation.Pose3D;
import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles;
import org.firstinspires.ftc.vision.apriltag.AprilTagGameDatabase;
import org.firstinspires.ftc.vision.apriltag.AprilTagLibrary;

import java.util.List;
import java.util.Vector;


@TeleOp
public class AprilTagLimeLight extends OpMode {

    private Limelight3A limelight;



    private IMU imu;

    public double normalizeAngle(double angle) {
        while (angle <= -Math.PI) angle += 2 * Math.PI;
        while (angle > Math.PI) angle -= 2 * Math.PI;
        return angle;

    }



    @Override
    public void init() {



        limelight = hardwareMap.get(Limelight3A.class, "limelight");
        limelight.pipelineSwitch(0);

        imu = hardwareMap.get(IMU.class, "imu");
        RevHubOrientationOnRobot orientation = new RevHubOrientationOnRobot(
                RevHubOrientationOnRobot.LogoFacingDirection.UP,
                RevHubOrientationOnRobot.UsbFacingDirection.RIGHT
        );
        imu.initialize(new IMU.Parameters(orientation));
        imu.resetYaw();
    }

    public void start() {
        limelight.start();
    }

    @Override
    public void loop() {


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
                double distance = fiducial.getRobotPoseTargetSpace().getPosition().y;
                double x = fiducial.getRobotPoseTargetSpace().getPosition().x;
                double z = fiducial.getRobotPoseTargetSpace().getPosition().z;
               VectorF target =  AprilTagGameDatabase.getDecodeTagLibrary().lookupTag(id).fieldPosition.multiplied(0.0254f);
                VectorF robotPose = new VectorF((float) botPose.getPosition().x, (float) botPose.getPosition().y, (float) botPose.getPosition().z);
                VectorF targetDis = target.subtracted(robotPose);
                telemetry.addLine("Id:" + id + "distance" + targetDis.magnitude());
                //telemetry.addLine("ID: " + id + " x " + x + " y: " + distance + " z: " + z);
            }


        }

        telemetry.update();
    }
}

