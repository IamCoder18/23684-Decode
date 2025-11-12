package org.firstinspires.ftc.teamcode.OpModes.Tuning;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import org.firstinspires.ftc.teamcode.Utilities.PIDFController;


@Config
@TeleOp
public class SpinderTuning extends OpMode{
    

        private DcMotor spindexerEncoder;
        private CRServo spindexer;
        private PIDFController pidfController;

        public static double P = 0.005, I = 0.0, D = 0.003, F = 0.0;
        public static int target = 0;

        public int ticksperDegree = 8192 / 360;

    public double normalizeAngle(double angle) {
        while (angle > Math.PI) angle -= 2 * Math.PI;
        while (angle < -Math.PI) angle += 2 * Math.PI;
        return angle;
    }

        @Override
        public void init() {
            spindexerEncoder = hardwareMap.get(DcMotor.class, "frontRight");
            spindexerEncoder.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

            // Make sure to configure the servo as "spindexerServo" in the robot configuration
            spindexer = hardwareMap.get(CRServo.class, "spindexer");

            pidfController = new PIDFController(P, I, D, F);
            telemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());
        }

        @Override
        public void loop() {
            pidfController.setPID(P, I, D, F);

            int currentPosition = spindexerEncoder.getCurrentPosition();
            int tar = target;
            double power = -pidfController.getOutput(currentPosition, tar);
            spindexer.setPower(power);

            telemetry.addData("Target", target);
            telemetry.addData("Current Position", currentPosition);
            telemetry.addData("Power", power);
            telemetry.addData("Error", target - currentPosition);
            telemetry.update();
        }
    

}
