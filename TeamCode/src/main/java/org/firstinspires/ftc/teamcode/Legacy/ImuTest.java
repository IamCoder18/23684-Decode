package org.firstinspires.ftc.teamcode.Legacy;

import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.IMU;

public class ImuTest extends OpMode {

    IMU imu;

    public RevHubOrientationOnRobot.LogoFacingDirection logoFacingDirection =
            RevHubOrientationOnRobot.LogoFacingDirection.RIGHT;
    public RevHubOrientationOnRobot.UsbFacingDirection usbFacingDirection =
            RevHubOrientationOnRobot.UsbFacingDirection.UP;

    @Override
    public void init() {
        imu = hardwareMap.get(IMU.class,"imu");

    }

    @Override
    public void loop() {

    }
}
