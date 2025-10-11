package org.firstinspires.ftc.teamcode.OpModes;

import android.os.Environment;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

@TeleOp(name = "TeleOp", group = "TeleOp")
public class FieldCentric extends OpMode {

    // Declare our motors
    DcMotor frontLeftMotor;
    DcMotor backLeftMotor;
    DcMotor frontRightMotor;
    DcMotor backRightMotor;

    // Retrieve the IMU from the hardware map
    IMU imu;

    // Log file constants and variable to store the pattern
    private static final String LOG_FILE_NAME = "ObeliskDetectionLog.csv";
    private static final String BASE_FOLDER_NAME = "FIRST";
    private String rememberedPattern = "NONE"; // Default pattern

    @Override
    public void init() {
        // Initialize motors
        // Make sure your ID's match your configuration
        frontLeftMotor = hardwareMap.dcMotor.get("frontLeftMotor");
        backLeftMotor = hardwareMap.dcMotor.get("backLeftMotor");
        frontRightMotor = hardwareMap.dcMotor.get("frontRightMotor");
        backRightMotor = hardwareMap.dcMotor.get("backRightMotor");

        // Reverse the right side motors.
        frontRightMotor.setDirection(DcMotorSimple.Direction.REVERSE);
        backRightMotor.setDirection(DcMotorSimple.Direction.REVERSE);

        // Initialize IMU
        imu = hardwareMap.get(IMU.class, "imu");
        IMU.Parameters parameters = new IMU.Parameters(new RevHubOrientationOnRobot(
                RevHubOrientationOnRobot.LogoFacingDirection.UP,
                RevHubOrientationOnRobot.UsbFacingDirection.FORWARD));
        imu.initialize(parameters);

        // Read the pattern from the log file
        File logFile = new File(Environment.getExternalStorageDirectory().getPath() + "/" + BASE_FOLDER_NAME + "/" + LOG_FILE_NAME);
        telemetry.addLine("Attempting to read pattern from: " + LOG_FILE_NAME);

        if (logFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(logFile))) {
                String line;
                String lastLine = "";
                while ((line = reader.readLine()) != null) {
                    if (!line.trim().isEmpty()) { // Consider only non-empty lines
                        lastLine = line;
                    }
                }
                if (!lastLine.isEmpty()) {
                    rememberedPattern = lastLine.trim();
                    telemetry.addLine("Pattern found: " + rememberedPattern);
                } else {
                    rememberedPattern = "EMPTY_LOG";
                    telemetry.addLine("Log file is empty. Pattern set to EMPTY_LOG.");
                }
            } catch (IOException e) {
                rememberedPattern = "ERROR_READING";
                telemetry.addLine("Error reading log file: " + e.getMessage());
            }
        } else {
            rememberedPattern = "NOT_FOUND";
            telemetry.addLine("Log file does not exist. Pattern set to NOT_FOUND.");
        }
        telemetry.update();
    }

    @Override
    public void init_loop() {
        // You can add telemetry updates here if needed during the init phase
    }

    @Override
    public void start() {
        // Code to run ONCE when the driver hits PLAY
        // imu.resetYaw(); // Optionally reset yaw on start, or keep button control
    }

    @Override
    public void loop() {
        double y = -gamepad1.left_stick_y; // Remember, Y stick value is reversed
        double x = gamepad1.left_stick_x;
        double rx = gamepad1.right_stick_x;

        // This button choice was made so that it is hard to hit on accident,
        // it can be freely changed based on preference.
        // The equivalent button is start on Xbox-style controllers.
        if (gamepad1.options) {
            imu.resetYaw();
        }

        double botHeading = imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.RADIANS);

        // Rotate the movement direction counter to the bot's rotation
        double rotX = x * Math.cos(-botHeading) - y * Math.sin(-botHeading);
        double rotY = x * Math.sin(-botHeading) + y * Math.cos(-botHeading);

        rotX = rotX * 1.1;  // Counteract imperfect strafing

        // Denominator is the largest motor power (absolute value) or 1
        // This ensures all the powers maintain the same ratio,
        // but only if at least one is out of the range [-1, 1]
        double denominator = Math.max(Math.abs(rotY) + Math.abs(rotX) + Math.abs(rx), 1);
        double frontLeftPower = (rotY + rotX + rx) / denominator;
        double backLeftPower = (rotY - rotX + rx) / denominator;
        double frontRightPower = (rotY - rotX - rx) / denominator;
        double backRightPower = (rotY + rotX - rx) / denominator;

        frontLeftMotor.setPower(frontLeftPower);
        backLeftMotor.setPower(backLeftPower);
        frontRightMotor.setPower(frontRightPower);
        backRightMotor.setPower(backRightPower);

        telemetry.addData("Remembered Pattern", rememberedPattern);
        telemetry.addData("Bot Heading (Radians)", "%.2f", botHeading);
        telemetry.addData("Gamepad Y", "%.2f", y);
        telemetry.addData("Gamepad X", "%.2f", x);
        telemetry.addData("Gamepad RX", "%.2f", rx);
        telemetry.update();
    }

    @Override
    public void stop() {
        // Code to run ONCE after the driver hits STOP
    }
}
