package org.firstinspires.ftc.teamcode.Subsystems;

import androidx.annotation.NonNull;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.Action;
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

        public double P = 0.005, I = 0.0, D = 0.0001, F = 0.0;
        public int target = 0;

        public double slot1 = 0 , slot2 = 120, slot3 = 240;



        public stupidSpindexer(HardwareMap hardwareMap) {
            spindexer = hardwareMap.get(CRServo.class, "Spindexer");
            spindexerEncoder = hardwareMap.get(DcMotorEx.class, "rearRight"); //TODO: check this
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
            // powers on motor, if it is not on
            spindexerEncoder.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            if (!initialized) {
                spindexer.setPower(1);
                initialized = true;
            }
            return zero.isPressed();
            // overall, the action powers the lift until it surpasses
            // 3000 encoder ticks, then powers it off
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

            if(!checked) {

                if (pos < slot3 || pos < slot1 && pos > slot2) {
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
            int normalPos = (int) normalizeAngle(spindexerEncoder.getCurrentPosition());

            int newSlot = (int) normalizeAngle(target);

            power = -pidfController.getOutput(normalPos, target);

            spindexer.setPower(power);


            return spindexer.getPower() < 0.2;
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
            return false;
        }
    }

    public Action MindlessSpindexer() {
        return new MindlessSpindexer();
    }



}
