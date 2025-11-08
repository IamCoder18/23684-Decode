package org.firstinspires.ftc.teamcode.Actions;

import androidx.annotation.NonNull;

import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.Action;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;

@Config
public class Shooter {

    private final DcMotor upperShooter;
    private final DcMotor lowerShooter;

    // Motor power constants
    public static double RUN_POWER = 1.0;
    public static double STOP_POWER = 0.0;
    
    // Offsets to resolve minor speed differences between motors
    public static double UPPER_OFFSET = 0.0;
    public static double LOWER_OFFSET = 0.0;

    public Shooter(HardwareMap hardwareMap) {
        upperShooter = hardwareMap.get(DcMotor.class, "upperShooter");
        lowerShooter = hardwareMap.get(DcMotor.class, "lowerShooter");
    }

    public Action run() {
        return packet -> {
            upperShooter.setPower(RUN_POWER + UPPER_OFFSET);
            lowerShooter.setPower(RUN_POWER + LOWER_OFFSET);
            return true;
        };
    }

    public Action stop() {
        return packet -> {
            upperShooter.setPower(STOP_POWER);
            lowerShooter.setPower(STOP_POWER);
            return true;
        };
    }
}
