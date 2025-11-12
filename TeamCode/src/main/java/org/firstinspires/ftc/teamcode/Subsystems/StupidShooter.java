package org.firstinspires.ftc.teamcode.Subsystems;

import androidx.annotation.NonNull;

import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.Action;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;

public class StupidShooter {

    DcMotorEx upperShooter;
    DcMotorEx lowerShooter;

    double averageRPM = 0;
    public static double TICKS_PER_REV = 28;

    double needForSpeed = 2080;

    public StupidShooter(HardwareMap hardwareMap) {
        upperShooter = hardwareMap.get(DcMotorEx.class, "upperShooter");
        lowerShooter = hardwareMap.get(DcMotorEx.class, "lowerShooter");

    }

    public void updateRPM() {
        double upperVelocity = upperShooter.getVelocity();
        double lowerVelocity = lowerShooter.getVelocity();

        double upperRPM = (upperVelocity / TICKS_PER_REV) * 60;
        double lowerRPM = (lowerVelocity / TICKS_PER_REV) * 60;

        averageRPM = (upperRPM + lowerRPM) / 2;
    }

    public void UpdateTelemetry(TelemetryPacket packet) {
        packet.put("Average RPM", averageRPM);
    }


    public class WindUp implements Action {
        boolean initialized = false;
        double timmythetime = 0;
        @Override
        public boolean run(@NonNull TelemetryPacket telemetryPacket) {
            updateRPM();

            timmythetime = timmythetime + 0.5;
            if (!initialized) {
                upperShooter.setPower(0.1 * Math.sin(timmythetime * 50) + 1);
                lowerShooter.setPower(1);
                initialized = true;
            }
            return averageRPM > needForSpeed;
        }
    }

    public Action WindUp() {
        return new WindUp();
    }

}
