package org.firstinspires.ftc.teamcode.Subsystems;

import androidx.annotation.NonNull;

import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.Action;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

public class StupidIntake {

    DcMotorEx intake;

    CRServo intakeDoorRight;
    CRServo intakeDoorLeft;

    public StupidIntake(HardwareMap hardwareMap) {
        intake = hardwareMap.get(DcMotorEx.class, "intake");
        intakeDoorRight = hardwareMap.get(CRServo.class, "intakeDoorRight");
        intakeDoorLeft = hardwareMap.get(CRServo.class, "intakeDoorLeft");
        intakeDoorLeft.setDirection(DcMotorSimple.Direction.REVERSE);

    }

    public class MainbrushIn implements Action {

        public boolean initialized = false;

        @Override
        public boolean run(@NonNull TelemetryPacket telemetryPacket) {
            if (!initialized) {
                intake.setVelocity(100);
                initialized = true;
            }
            return false;
        }
    }

    public Action mainbrushIn(){
        return new MainbrushIn();
    }

    public class MainbrushStop implements Action {

        public boolean initialized = false;

        @Override
        public boolean run(@NonNull TelemetryPacket telemetryPacket) {
            if (!initialized) {
                intake.setVelocity(0);
                initialized = true;
            }
            return false;
        }
    }

    public Action mainbrushStop(){
        return new MainbrushStop();
    }

    public class IntakeDoorIn implements Action {

        public boolean initialized = false;

        @Override
        public boolean run(@NonNull TelemetryPacket telemetryPacket) {
            if (!initialized) {
                intakeDoorLeft.setPower(1);
                intakeDoorRight.setPower(1);
                initialized = true;
            }
            return !true;
        }
    }

    public Action intakeDoorIn(){
        return new IntakeDoorIn();
    }



}
