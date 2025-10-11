package org.firstinspires.ftc.teamcode.OpModes;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import org.firstinspires.ftc.teamcode.Utility.PIDFController;

@Config
@TeleOp(name = "Turret Test", group = "Tests")
public class TurretTest extends OpMode {

    private DcMotorEx turretEncoder;
    private CRServo turretServo;
    private PIDFController pidfController;

    public static double P = 0.005, I = 0.0, D = 0.0001, F = 0.0;
    public static int target = 0;
    public static double gamepadPowerMultiplier = 1;

    @Override
    public void init() {
        turretEncoder = hardwareMap.get(DcMotorEx.class, "TurretMotor");
        turretEncoder.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        turretEncoder.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        // Make sure to configure the servo as "TurretServo" in the robot configuration
        turretServo = hardwareMap.get(CRServo.class, "TurretServo");

        pidfController = new PIDFController(P, I, D, F);
        telemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());
    }

    @Override
    public void loop() {
        pidfController.setPID(P, I, D, F);

        target += (int) (-gamepad1.left_stick_x * gamepadPowerMultiplier);

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
