package org.firstinspires.ftc.teamcode.OpModes.TeleOp;

import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.IMU;


@TeleOp
public class BasicOfTheBasic extends OpMode {

    DcMotor frontRight, frontLeft,rearRight, rearLeft;

     IMU imu;

    @Override
    public void init() {
        frontRight = hardwareMap.get(DcMotor.class,"frontLeft");
        frontLeft = hardwareMap.get(DcMotor.class,"frontRight");
    rearLeft = hardwareMap.get(DcMotor.class,"rearLeft");
    rearRight = hardwareMap.get(DcMotor.class,"rearRight");

    rearLeft.setDirection(DcMotor.Direction.REVERSE);
    frontLeft.setDirection(DcMotor.Direction.REVERSE);

    frontRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
    frontLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
    rearRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
    rearLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

    imu = hardwareMap.get(IMU.class, "imu");
        RevHubOrientationOnRobot orientation = new RevHubOrientationOnRobot(
                RevHubOrientationOnRobot.LogoFacingDirection.LEFT,
                RevHubOrientationOnRobot.UsbFacingDirection.UP
        );
        imu.initialize(new IMU.Parameters(orientation));


    }

    @Override
    public void loop() {
        double forward = -gamepad1.left_stick_y;
        double strafe = gamepad1.left_stick_x;
        double turn = gamepad1.right_stick_x;

        double frontLeftPower = forward + strafe + turn;
        double rearLeftPower = forward - strafe + turn;
        double frontRightPower = forward - strafe - turn;
        double rearRightPower = forward + strafe - turn;

        double maxpower = 1;




    }
}
