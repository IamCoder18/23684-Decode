package org.firstinspires.ftc.teamcode.Actions;

import androidx.annotation.NonNull;

import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.Action;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;

@Config
public class Intake {

    private final DcMotor intake;

    // Motor power constants
    public static double IN_POWER = 1.0;
    public static double OUT_POWER = -1.0;
    public static double STOP_POWER = 0.0;

    public Intake(HardwareMap hardwareMap) {
        intake = hardwareMap.get(DcMotor.class, "intake");
    }

    public Action in() {
        return packet -> {
            intake.setPower(IN_POWER);
            return true;
        };
    }

    public Action out() {
        return packet -> {
            intake.setPower(OUT_POWER);
            return true;
        };
    }

    public Action stop() {
        return packet -> {
            intake.setPower(STOP_POWER);
            return true;
        };
    }
}
