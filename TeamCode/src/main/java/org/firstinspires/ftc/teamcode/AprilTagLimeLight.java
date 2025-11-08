package org.firstinspires.ftc.teamcode;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.IMU;

import org.firstinspires.ftc.robotcore.external.navigation.Pose3D;
import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles;


@TeleOp
public class AprilTagLimeLight extends OpMode {

    private Limelight3A limelight;

    private IMU imu;

    private DcMotorEx turretEncoder;
    private CRServo turretServo;
    private PIDFController pidfController;


    String team = "BLU";

    public static double P = 0.005, I = 0.0, D = 0.0001, F = 0.0;

     double REDGOALx = -60, REDGOALy = 60;
    double BLUGOALx = -60, BLUGOALy = 60;

    double target;

    public double normalizeAngle(double angle) {
        while (angle <= -Math.PI) angle += 2 * Math.PI;
        while (angle > Math.PI) angle -= 2 * Math.PI;
        return angle;
    }

    public double RedAngle(double robotX,double robotY) {


        double vx =  (REDGOALx / 39.3700787 ) - robotX;
        double vy = (REDGOALy / 39.3700787) - robotY;

        double theta = imu.getRobotYawPitchRollAngles().getYaw();


        double cosA = Math.cos(-theta);
        double sinA = Math.sin(-theta);

        double vrx = cosA * vx - sinA * vy;
        double vry = sinA * vx + cosA * vy;


        double desired = normalizeAngle(Math.atan2(vry, vrx));

        return desired;
    }

    public double BluAngle(double robotX,double robotY) {


        double vx =  (BLUGOALx / 39.3700787 ) - robotX;
        double vy = (BLUGOALy / 39.3700787) - robotY;

        double theta = imu.getRobotYawPitchRollAngles().getYaw();


        double cosA = Math.cos(-theta);
        double sinA = Math.sin(-theta);

        double vrx = cosA * vx - sinA * vy;
        double vry = sinA * vx + cosA * vy;



        double desired = normalizeAngle(Math.atan2(vry, vrx));

        return desired;
    }


//    public double RedAngle(double x, double y){
//        double diffx = (REDGOALx / 39.3700787 ) - x;
//        double diffy = (REDGOALy / 39.3700787) - y;
//
//        double angle = Math.atan2(diffy, diffx);
//
//        return Math.toDegrees(angle);
//
//    }
//
//    public double BluAngle(double x, double y){
//        double diffx = (BLUGOALx / 39.3700787) - x;
//        double diffy = (BLUGOALy / 39.3700787) - y;
//
//        double angle = Math.atan2(diffy, diffx);
//
//        return Math.toDegrees(angle);
//
//    }

    @Override
    public void init() {

        pidfController = new PIDFController(P,I,D,F);

        turretEncoder = hardwareMap.get(DcMotorEx.class, "ET");
        turretServo = hardwareMap.get(CRServo.class, "ST");


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
            Pose3D botPose = llResult.getBotpose_MT2();
            telemetry.addData("Tx", llResult.getTx());
            telemetry.addData("Ty", llResult.getTy());
            telemetry.addData("Ta", llResult.getTa());
            telemetry.addData("BotPose", botPose.toString());
            telemetry.addData("Orientation", botPose.getOrientation().toString());
            telemetry.addData("ID", llResult.getFiducialResults());

            telemetry.addData("imuing", imu.getRobotYawPitchRollAngles());

            if (team == "RED") {
                target = RedAngle(botPose.getPosition().x, botPose.getPosition().y);
            } else if (team == "BLU") {
                target = BluAngle(botPose.getPosition().x, botPose.getPosition().y);
            }


        }




        pidfController.setPID(P, I, D, F);


        int currentPosition = turretEncoder.getCurrentPosition();
        double power = -pidfController.getOutput(currentPosition, target);
        turretServo.setPower(power);

        telemetry.addData("Target", target);
        telemetry.addData("Current Position", currentPosition);
        telemetry.addData("Power", power);
        telemetry.addData("Error", target - currentPosition);
        telemetry.update();
    }
}

