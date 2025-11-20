package org.firstinspires.ftc.teamcode.OpModes.Tuning;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import org.firstinspires.ftc.teamcode.Utilities.PIDFController;

@Config
@TeleOp
public class ShooterPIDF extends OpMode {

    public static double targetU = 0, targetL = 0;
    public static UPPER upper = new UPPER();
    public static LOWER lower = new LOWER();
    PIDFController UController;
    PIDFController LController;

    DcMotorEx upperShooter;
    DcMotorEx lowerShooter;

    double upperRPM = 0;
    double lowerRPM = 0;

    double TICKS_PER_REV = 28;

    @Override
    public void init() {


      upperShooter = hardwareMap.get(DcMotorEx.class, "upperShooter");
      lowerShooter = hardwareMap.get(DcMotorEx.class, "lowerShooter");

      UController = new PIDFController(upper.p, upper.i, upper.d, upper.f);
      LController = new PIDFController(lower.p, lower.i, lower.d, lower.f);

        UController.setOutputLimits(0, 1);
        LController.setOutputLimits(0,1);



    }

    @Override
    public void loop() {


        UController.setPID(upper.p, upper.i, upper.d, upper.f);
        LController.setPID(lower.p, lower.i, lower.d, lower.f);



        double upperVelocity = upperShooter.getVelocity();
        double lowerVelocity = lowerShooter.getVelocity();
        upperRPM = (upperVelocity / TICKS_PER_REV) * 60;
        lowerRPM = (lowerVelocity / TICKS_PER_REV) * 60;

        double upperPower = UController.getOutput(upperRPM, targetU);
        double lowerPower = LController.getOutput(lowerRPM, targetL);

        upperShooter.setPower(upperPower);
        lowerShooter.setPower(lowerPower);

        telemetry.addData("upperRPM", upperRPM);
        telemetry.addData("lowerRPM", lowerRPM);
        telemetry.update();


    }

    public static class UPPER {
        public static double p = 0, i = 0, d = 0, f = 0;

    }

    public static class LOWER {
        public static double p = 0, i = 0, d = 0, f = 0;

    }

}
