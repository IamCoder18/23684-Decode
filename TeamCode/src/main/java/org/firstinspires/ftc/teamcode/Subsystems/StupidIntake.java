package org.firstinspires.ftc.teamcode.Subsystems;

import androidx.annotation.NonNull;

import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.Action;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;

public class StupidIntake {

    DcMotorEx intake;

    CRServo intakeDoorRight;
    CRServo intakeDoorLeft;

    public StupidIntake(HardwareMap hardwareMap) {
        intake = hardwareMap.get(DcMotorEx.class, "intake");
        intakeDoorRight = hardwareMap.get(CRServo.class, "intakeDoorRight");
        intakeDoorLeft = hardwareMap.get(CRServo.class, "intakeDoorLeft");

    }

    public class revUp implements Action {

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


}
