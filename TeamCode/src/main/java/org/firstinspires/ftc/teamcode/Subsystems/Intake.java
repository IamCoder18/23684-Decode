package org.firstinspires.ftc.teamcode.Subsystems;

import androidx.annotation.NonNull;

import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.Action;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;

@Config
public class Intake {

    private static Intake instance = null;

    private DcMotor intake;

    // Motor power constants
    public static double IN_POWER = 1.0;
    public static double OUT_POWER = -1.0;
    public static double STOP_POWER = 0.0;

    private Intake() {
    }

    public static void initialize(HardwareMap hardwareMap) {
        if (instance == null) {
            instance = new Intake();
            instance.intake = hardwareMap.get(DcMotor.class, "intake");
        }
    }

    public static Intake getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Intake not initialized. Call initialize(hardwareMap) first.");
        }
        return instance;
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

    public static void shutdown() {
        // No cleanup needed currently
    }
}
