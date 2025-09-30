package org.firstinspires.ftc.teamcode.PidfDrive;

import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.Vector2d;
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;
import com.qualcomm.robotcore.hardware.IMU;
import org.firstinspires.ftc.teamcode.Localizer;


public class DrivePIDF {


    double Xp = 0, Xi = 0, Xd = 0, Xf = 0;
    double Yp = 0, Yi = 0, Yd = 0, Yf = 0;
    double Hp = 0, Hi = 0, Hd = 0, Hf = 0;

    Localizer localizer;

    Pose2d CurentPos;

    PIDFController  pidfX = new PIDFController(Xp,Xi,Xd,Xf);
    PIDFController  pidfY = new PIDFController(Yp,Yi,Yd,Yf);
    PIDFController  pidfH = new PIDFController(Hp,Hi,Hd,Hf);

    DcMotor frontLeft;
    DcMotor frontRight;
    DcMotor rearLeft;
    DcMotor rearRight;

    public double startX = 0 , startY = 0 ,startH = 0;

    public DrivePIDF(HardwareMap hw, Localizer local) {
        this.localizer = local;

        frontRight = hw.get(DcMotor.class,"frontRight");
        frontLeft = hw.get(DcMotor.class, "frontLeft");
        rearRight = hw.get(DcMotor.class, "rearRight");
        rearLeft = hw.get(DcMotor.class,"rearLeft");

        frontRight.setDirection(DcMotorSimple.Direction.REVERSE);
        rearRight.setDirection(DcMotorSimple.Direction.REVERSE);

        frontRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        frontLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rearRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rearLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        IMU imu = hw.get(IMU.class, "imu");
        IMU.Parameters parameters = new IMU.Parameters(new RevHubOrientationOnRobot(
                RevHubOrientationOnRobot.LogoFacingDirection.UP,
                RevHubOrientationOnRobot.UsbFacingDirection.FORWARD));
        imu.initialize(parameters);
    }

    public double normalizeAngle(double angle) {
        while (angle > Math.PI) angle -= 2 * Math.PI;
        while (angle < -Math.PI) angle += 2 * Math.PI;
        return angle;
    }

    public void setStartPos(double X, double Y, double H){
        startX = X;
        startY = Y;
        startH = H;
    }

    public void GoToPos(double x, double y , double Heading){
        CurentPos = new Pose2d(new Vector2d(localizer.getPose().position.x + startX,localizer.getPose().position.y + startY),localizer.getPose().heading.toDouble() + startH);

        double powerX = pidfX.getOutput(CurentPos.position.x, x);
        double powerY = pidfY.getOutput(CurentPos.position.y, y);
        double powerH = pidfH.getOutput(normalizeAngle(CurentPos.heading.toDouble()),normalizeAngle(Heading));

        double rotX = powerX * Math.cos(-CurentPos.heading.toDouble()) - powerY * Math.sin(-CurentPos.heading.toDouble());
        double rotY = powerX * Math.sin(-CurentPos.heading.toDouble()) + powerY * Math.cos(-CurentPos.heading.toDouble());

        rotX = rotX * 1.1;  // Counteract imperfect strafing

        // Denominator is the largest motor power (absolute value) or 1
        // This ensures all the powers maintain the same ratio,
        // but only if at least one is out of the range [-1, 1]
        double denominator = Math.max(Math.abs(rotY) + Math.abs(rotX) + Math.abs(powerH), 1);
        double frontLeftPower = (rotY + rotX + powerH) / denominator;
        double backLeftPower = (rotY - rotX + powerH) / denominator;
        double frontRightPower = (rotY - rotX - powerH) / denominator;
        double backRightPower = (rotY + rotX - powerH) / denominator;

        frontLeft.setPower(frontLeftPower);
        rearLeft.setPower(backLeftPower);
        frontRight.setPower(frontRightPower);
        rearRight.setPower(backRightPower);



    }
}
