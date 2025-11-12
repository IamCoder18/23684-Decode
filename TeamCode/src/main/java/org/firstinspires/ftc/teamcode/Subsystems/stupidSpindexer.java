package org.firstinspires.ftc.teamcode.Subsystems;

import androidx.annotation.NonNull;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.Action;
import com.acmerobotics.roadrunner.InstantAction;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.TouchSensor;

import org.firstinspires.ftc.teamcode.Utilities.PIDFController;


public class stupidSpindexer {

        CRServo spindexer;
        DcMotor spindexerEncoder;

        TouchSensor zero;

        PIDFController pidfController;

         public int pos;

        public double P = 0.005, I = 0.0, D = 0.003, F = 0.00001;;
        public int target = 0;

        public double slot1 = 0 , slot2 = 8192 * (1/3), slot3 = 8192 * (2/3);

         public int ticksperDegree = 8192 / 360;



        public stupidSpindexer(HardwareMap hardwareMap) {
            spindexer = hardwareMap.get(CRServo.class, "spindexer");
            spindexerEncoder = hardwareMap.get(DcMotorEx.class, "frontRight"); //TODO: check this
            zero = hardwareMap.get(TouchSensor.class, "spindexerZero");

            spindexerEncoder.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            spindexerEncoder.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);


            pidfController = new PIDFController(P, I, D, F);

            pidfController.setPID(P, I, D, F);


        }

    public double normalizeAngle(double angle) {
        while (angle > Math.PI) angle -= 2 * Math.PI;
        while (angle < -Math.PI) angle += 2 * Math.PI;
        return angle;
    }

    public class Zero implements Action {
        private boolean initialized = false;
        @Override
        public boolean run(@NonNull TelemetryPacket packet) {

            spindexerEncoder.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            if (!initialized) {
                spindexer.setPower(1);
                initialized = true;
            }
            return !zero.isPressed();

        }
    }

    public Action Zero() {
        return new Zero();
    }



    public class NextSlot implements Action{
            double power;
            boolean checked = false;
        @Override
        public boolean run(@NonNull TelemetryPacket telemetryPacket) {

            pos = spindexerEncoder.getCurrentPosition();
            spindexerEncoder.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

            if(!checked) {

                if ( pos < slot1 && pos > slot2) {
                    target = (int) slot2;
                    checked = true;
                }
                if (pos < slot1 && pos < slot2 && pos > slot3) {
                    target = (int) slot3    ;
                    checked = true;
                }
                if (pos < slot2 && pos < slot1 && pos > slot3) {
                    target = (int) slot1;
                    checked = true;
                }

            }
            int normalPos = spindexerEncoder.getCurrentPosition();
            power = -pidfController.getOutput(normalPos, target);

            spindexer.setPower(power);

            boolean duoble = normalPos - target < 50 && normalPos - target > -50;


            return duoble;
        }
    }

    public Action NextSlot() {
        return new NextSlot();
    }

    public class MindlessSpindexer implements Action{
        boolean initialized = false;
        @Override
        public boolean run(@NonNull TelemetryPacket telemetryPacket) {

            if (!initialized) {
                spindexer.setPower(1);
                initialized = true;
            }
            return true;
        }
    }

    public Action MindlessSpindexer() {
        return new InstantAction(() -> {
            spindexer.setPower(1);
        });
    }



}
